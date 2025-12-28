package re.limus.timas.hook.items.message.helper

import re.limus.timas.hook.utils.XLog
import top.sacz.xphelper.XpHelper.classLoader
import top.sacz.xphelper.dexkit.DexFinder
import top.sacz.xphelper.ext.toClass
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * 本地灰字提示封装：支持 _text/_member/_url 节点，反射调用 TIM 的 addLocalJsonGrayTipMsg
 */
object LocalGrayTips {

    sealed interface Node {
        data class Text(val text: String, val col: String = "1") : Node
        data class Member(val uid: String, val uin: String, val nick: String, val col: String = "3") : Node
        data class MsgRef(val text: String, val seq: Long, val col: String = "3") : Node
    }

    private val msgConstCls by lazy { "com.tencent.qqnt.kernel.nativeinterface.MsgConstant".toClass() }
    private val busiCls by lazy { "com.tencent.qqnt.kernel.nativeinterface.JsonGrayBusiId".toClass() }
    private val contactCls by lazy { "com.tencent.qqnt.kernelpublic.nativeinterface.Contact".toClass() }
    private val elementCls by lazy { "com.tencent.qqnt.kernelpublic.nativeinterface.JsonGrayElement".toClass() }

    private val contactCtor by lazy {
        DexFinder.findMethod {
            declaredClass = contactCls
            paramCount = 3
            parameters = arrayOf(Int::class.java, String::class.java, String::class.java)
        }.firstConstructorOrNull() ?: DexFinder.findMethod {
            declaredClass = contactCls
            paramCount = 2
            parameters = arrayOf(Int::class.java, String::class.java)
        }.firstConstructorOrNull()
    }

    private val elementCtor by lazy {
        val xmlParamCls = runCatching { "com.tencent.qqnt.kernelpublic.nativeinterface.XmlToJsonParam".toClass() }.getOrNull()
        elementCls.getConstructor(Long::class.javaPrimitiveType, String::class.java, String::class.java, Boolean::class.javaPrimitiveType, xmlParamCls)
    }

    private var cachedAddMethod: Method? = null
    private var cachedProxy: Any? = null

    /**
     * 发送本地灰字（通过 wrapperSession -> msgService -> addLocalJsonGrayTipMsg）
     * @param wrapperSession IQQNTWrapperSession 实例（onMsfPush 的 thisObject）
     * @param chatTypeField  "KCHATTYPEGROUP" 或 "KCHATTYPEC2C"
     * @param id             群ID（群聊）或 uid（单聊）
     * @param items          节点列表：Text/Member/MsgRef
     * @param recentAbstract 最近会话摘要文本
     */
    fun send(wrapperSession: Any, chatTypeField: String, id: String, items: List<Node>, recentAbstract: String) {
        runCatching {
            val msgService = wrapperSession.javaClass.getMethod("getMsgService").invoke(wrapperSession) ?: return
            val chatType = msgConstCls.getField(chatTypeField).getInt(null)

            val contact = if (contactCtor.parameterTypes.size == 3) {
                contactCtor.newInstance(chatType, id, "")
            } else {
                contactCtor.newInstance(chatType, id)
            }

            val busiFieldName = if (chatTypeField == "KCHATTYPEGROUP") "AIO_AV_GROUP_NOTICE" else "AIO_AV_C2C_NOTICE"
            val busiId = runCatching { busiCls.getField(busiFieldName).getInt(null) }
                .getOrElse { busiCls.getField("AIO_ROBOT_SAFETY_TIP").getInt(null) }

            val json = """{"align":"center","items":[${items.joinToString(",") { it.toJson() }}]}"""
            val element = elementCtor.newInstance(busiId.toLong(), json, recentAbstract, false, null)

            val addMethod = cachedAddMethod ?: DexFinder.findMethod {
                declaredClass = msgService.javaClass
                methodName = "addLocalJsonGrayTipMsg"
                paramCount = 5
            }.firstOrNull()?.also { cachedAddMethod = it } ?: return

            runCatching {
                addMethod.invoke(msgService, contact, element, true, true, null)
            }.onFailure {
                val cbType = addMethod.parameterTypes[4]
                val proxy = cachedProxy ?: Proxy.newProxyInstance(classLoader, arrayOf(cbType)) { _, _, _ -> null }.also { cachedProxy = it }
                addMethod.invoke(msgService, contact, element, true, true, proxy)
            }
        }.onFailure { XLog.e(it) }
    }

    private fun Node.toJson(): String = when (this) {
        is Node.Text -> """{"_type":"_text","type":"nor","txt":"${text.esc()}","col":"$col"}"""
        is Node.Member -> """{"_type":"_member","type":"qq","uid":"$uid","jp":"$uid","uin":"$uin","tp":"0","nm":"${nick.esc()}","col":"$col"}"""
        is Node.MsgRef -> """{"_type":"_url","type":"url","txt":"${text.esc()}","local_jp":58,"param":{"seq":$seq},"col":"$col"}"""
    }

    private fun String.esc() = replace("\"", "\\\"")
}