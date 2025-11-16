package io.live.timas.hook.items.message

import android.content.Context
import io.live.timas.annotations.RegisterToUI
import io.live.timas.annotations.UiCategory
import io.live.timas.hook.base.SwitchHook
import top.sacz.xphelper.ext.toClass
import top.sacz.xphelper.reflect.MethodUtils

@RegisterToUI
object FlashPic2Normal : SwitchHook() {

    override val name = "令 闪照 像正常图片一样显示"

    override val category = UiCategory.MESSAGE

    override fun onHook(ctx: Context, loader: ClassLoader) {
        "com.tencent.mobileqq.aio.msglist.AIOMsgItemFactoryProvider".toClass().declaredMethods.first {
            it.returnType != Void.TYPE
                    && it.parameterCount == 1 && it.parameterTypes[0] == Integer.TYPE
        }.hookAfter {
            val id = args[0] as Int
            if (id == 84) {
                result = MethodUtils.create(thisObject)
                    .invokeFirst(thisObject, 5)
            } else if (id == 85) {
                result = MethodUtils.create(thisObject)
                    .invokeFirst(thisObject, 4)
            }
        }
    }
}