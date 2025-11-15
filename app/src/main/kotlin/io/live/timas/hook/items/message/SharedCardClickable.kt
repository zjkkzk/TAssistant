package io.live.timas.hook.items.message

import android.content.Context
import io.live.timas.annotations.RegisterToUI
import io.live.timas.annotations.UiCategory
import io.live.timas.hook.base.SwitchHook
import top.sacz.xphelper.dexkit.DexFinder
import top.sacz.xphelper.ext.toClass

@RegisterToUI
object SharedCardClickable : SwitchHook() {

    override val name = "正常打开别人分享的卡片消息"

    override val description = "去除点击别人分享的 卡片/链接 时版本过低的提示，并能够正常查看"

    override val category = UiCategory.MESSAGE

    override fun onHook(ctx: Context, loader: ClassLoader) {
        DexFinder.findMethod {
            declaredClass = "com.tencent.mobileqq.aio.msglist.holder.component.ark.d".toClass()
            methodName = "a"
            parameters = arrayOf(String::class.java, String::class.java)
            returnType = Boolean::class.java
        }.hookAfter {
            result = true
        }
    }
}