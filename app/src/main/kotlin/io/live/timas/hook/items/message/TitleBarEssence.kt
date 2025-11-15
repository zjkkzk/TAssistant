package io.live.timas.hook.items.message

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.children
import io.live.timas.annotations.RegisterToUI
import io.live.timas.annotations.UiCategory
import io.live.timas.api.ContactUtils
import io.live.timas.api.TIMContactUpdateListener
import io.live.timas.hook.base.SwitchHook
import io.live.timas.hook.utils.XLog
import top.sacz.xphelper.dexkit.DexFinder
import top.sacz.xphelper.ext.toClass

@RegisterToUI
object TitleBarEssence : SwitchHook() {

    override val name = "TIM 群标题栏添加精华消息入口"

    override val description = "仅适配 TIM_NT"

    override val category = UiCategory.MESSAGE
    private val Layout_Id = "TitleBarEssence".hashCode()

    override fun onHook(ctx: Context, loader: ClassLoader) {
        DexFinder.findMethod {
            declaredClass = "com.tencent.tim.aio.titlebar.TimRight1VB".toClass()
            returnType = "com.tencent.mobileqq.aio.widget.RedDotImageView".toClass()
        }.hookAfter {
            val view = result as View
            val rootView = view.parent as ViewGroup

            if (!rootView.children.map { it.id }.contains(Layout_Id)) {
                val imageView = ImageView(view.context).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        dp(context, 20f),
                        dp(context, 20f)
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                        addRule(RelativeLayout.CENTER_VERTICAL)
                        marginEnd = dp(view.context, 70f)
                    }
                    id = Layout_Id
                    val iconResId = context.resources.getIdentifier(
                        "qui_tui_brand_products",
                        "drawable",
                        context.packageName
                    )
                    setImageResource(iconResId)
                    val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                    setColorFilter(if (night) Color.WHITE else Color.BLACK)
                }
                imageView.setOnClickListener {
                    val troopUin = getCurrentGroupUin()
                    try {
                        val browser = loader.loadClass("com.tencent.mobileqq.activity.QQBrowserDelegationActivity")
                        it.context.startActivity(
                            Intent(it.context, browser).apply {
                                putExtra("fling_action_key", 2)
                                putExtra("fling_code_key", it.context.hashCode())
                                putExtra("useDefBackText", true)
                                putExtra("param_force_internal_browser", true)
                                putExtra("url", "https://qun.qq.com/essence/index?gc=$troopUin")
                            }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // 仅在群聊显示：先定义检查函数，再添加视图并调用
                fun checkAndUpdateVisibility() {
                    val aio = try {
                        TIMContactUpdateListener.getCurrentAIOContact()
                    } catch (_: Throwable) {
                        null
                    }
                    val getUin = aio?.let { ContactUtils.getGroupUinFromAIOContact(it) }
                    imageView.visibility =
                        if (getUin.isNullOrEmpty()) View.GONE else View.VISIBLE
                }
                rootView.addView(imageView)
                checkAndUpdateVisibility()
                imageView.post { checkAndUpdateVisibility() }
            }
        }
    }
    fun getCurrentGroupUin(): String? {
        val aio = try {
            TIMContactUpdateListener.getCurrentAIOContact()
        } catch (e: Throwable) {
            XLog.e(e)
        }
        return aio.let { ContactUtils.getGroupUinFromAIOContact(it) }
    }
    fun dp(ctx: Context, value: Float): Int =
        (ctx.resources.displayMetrics.density * value + 0.5f).toInt()
}