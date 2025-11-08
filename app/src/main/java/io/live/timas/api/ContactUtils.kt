package io.live.timas.api

import io.live.timas.api.TIMContactUpdateListener.Companion.getCurrentAIOContact
import io.live.timas.api.TIMEnvTool.getUidFromUin
import io.live.timas.api.TIMEnvTool.getUinFromUid
import top.sacz.xphelper.reflect.ClassUtils
import top.sacz.xphelper.reflect.ConstructorUtils
import top.sacz.xphelper.reflect.FieldUtils


class ContactUtils {

    fun getCurrentContact(): Any {
        val currentAIOContact = getCurrentAIOContact()
        return getContactByAIOContact(currentAIOContact)
    }

    fun getContactByAIOContact(aioContact: Any): Any {
        val peerUid = FieldUtils.create(aioContact)
            .fieldName("e")
            .fieldType(String::class.java)
            .firstValue<String?>(aioContact)
        val chatType: Int = FieldUtils.create(aioContact)
            .fieldName("d")
            .fieldType(Int::class.javaPrimitiveType)
            .firstValue(aioContact)!!
        val guild = FieldUtils.create(aioContact)
            .fieldName("f")
            .fieldType(String::class.java)
            .firstValue<String?>(aioContact)
        val nick = FieldUtils.create(aioContact)
            .fieldName("g")
            .fieldType(String::class.java)
            .firstValue<String?>(aioContact)
        return getContact(chatType, peerUid, guild)
    }

    /**
     * 从 AIOContact 提取群号（非群聊或失败返回 null）。
     */
    fun getGroupUinFromAIOContact(aioContact: Any): String? {
        try {
            val chatType: Int = FieldUtils.create(aioContact)
                .fieldName("d")
                .fieldType(Int::class.javaPrimitiveType)
                .firstValue(aioContact)!!
            val peerUid = FieldUtils.create(aioContact)
                .fieldName("e")
                .fieldType(String::class.java)
                .firstValue<String?>(aioContact)
            if (chatType != 2 || peerUid == null || peerUid.isEmpty()) return null
            if (isNumeric(peerUid)) return peerUid
            return try {
                getUinFromUid(peerUid)
            } catch (_: Throwable) {
                null
            }
        } catch (_: Throwable) {
            return null
        }
    }


    /**
     * 获取好友聊天对象
     */
    fun getFriendContact(uin: String?): Any {
        return getContact(1, uin)
    }

    /**
     * 获取群聊聊天对象
     */
    fun getGroupContact(troopUin: String?): Any {
        return getContact(2, troopUin)
    }

    /**
     * 获取聊天对象
     *
     * @param type 联系人类型 2是群聊 1是好友
     * @param uin  正常的QQ号/群号
     */
    fun getContact(type: Int, uin: String?): Any {
        return getContact(type, uin, "")
    }

    /**
     * 获取聊天对象 包含可能是频道的情况
     *
     * @param type    type为4时创建频道聊天对象
     * @param guildId 频道id
     */
    fun getContact(type: Int, uin: String?, guildId: String?): Any {
        val contactClass =
            ClassUtils.findClass("com.tencent.qqnt.kernelpublic.nativeinterface.Contact")
        try {
            val peerUid = if (type != 2 && type != 4 && isNumeric(uin!!)) getUidFromUin(uin) else uin
            return ConstructorUtils.newInstance(
                contactClass,
                arrayOf<Class<*>>(
                    Int::class.javaPrimitiveType!!,
                    String::class.java,
                    String::class.java
                ),
                type,
                peerUid,
                guildId
            )
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun isNumeric(str: String): Boolean {
        var i = str.length
        while (--i >= 0) {
            val chr = str[i].code
            if (chr !in 48..57) return false
        }
        return true
    }

}