package io.live.timas.hook.items.message

import android.content.Context
import io.live.timas.annotations.RegisterToUI
import io.live.timas.annotations.UiCategory
import io.live.timas.hook.base.SwitchHook
import top.sacz.xphelper.reflect.MethodUtils

@RegisterToUI
object SystemEmojiStyle : SwitchHook() {

    override val name = "使用系统 Emoji 样式"

    override val description = "如题所示"

    override val category = UiCategory.MESSAGE

    override fun onHook(ctx: Context, loader: ClassLoader) {
        val emojiClass = "com.tencent.mobileqq.text.EmotcationConstants"

        MethodUtils.create(emojiClass)
            .params(Int::class.java)
            .returnType(Int::class.java)
            .first()
            .hookAfter {
                result = -1
            }

        MethodUtils.create(emojiClass)
            .params(Int::class.java, Int::class.java)
            .returnType(Int::class.java)
            .first()
            .hookAfter {
                result = -1
            }
    }
}