package re.limus.timas.hook.items.message.core

import android.view.View
import android.view.View.OnClickListener
import re.limus.timas.api.TIMSendMsgTool
import top.sacz.xphelper.reflect.FieldUtils

class RepeatMessageClickListener(private val msgRecord: Any, private val contact: Any?) :
    OnClickListener {
    private var elements: ArrayList<Any?> = FieldUtils.create(msgRecord)
        .fieldName("elements")
        .firstValue(msgRecord)

    override fun onClick(v: View?) {
        rereading()
    }

    fun rereading() {
        if (forward()) return
        TIMSendMsgTool.sendMsg(contact,elements)
    }
    private fun forward(): Boolean {
        try {
            var isForwardMsg = false
            for (element in this.elements) {
                val elementType: Int =
                    FieldUtils.create(element)
                        .fieldName("elementType")
                        .fieldType(Int::class.javaPrimitiveType)
                        .firstValue(element)
                if (elementType == 2 || elementType == 5 || elementType == 10) {
                    isForwardMsg = true
                    break
                }
            }
            if (isForwardMsg) {
                val msgId: Long =
                    FieldUtils.create(msgRecord).fieldName("msgId")
                        .fieldType(Long::class.javaPrimitiveType).firstValue(msgRecord)
                val msgIdList = ArrayList<Long?>()
                msgIdList.add(msgId)
                val targetContactList = ArrayList<Any?>()
                targetContactList.add(contact)
                TIMSendMsgTool.forwardMsg(msgIdList, contact, targetContactList)
                return true
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return false
    }
}