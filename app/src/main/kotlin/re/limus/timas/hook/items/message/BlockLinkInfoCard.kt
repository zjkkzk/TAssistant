package re.limus.timas.hook.items.message

import android.content.Context
import re.limus.timas.annotations.RegisterToUI
import re.limus.timas.annotations.UiCategory
import re.limus.timas.hook.base.SwitchHook

// dartcv
@RegisterToUI
object BlockLinkInfoCard: SwitchHook() {
    
    override val name = "屏蔽链接信息卡片"
    
    override val category = UiCategory.MESSAGE

    override fun onHook(ctx: Context, loader: ClassLoader) {
        DexFinder.findMethod {
            declaredClass = "com.tencent.qqnt.kernel.nativeinterface.LinkInfo".toClass()
        }.hookConstructorBefore {
            if (args[0] != null) { 
                result = null
            }
        }
    }
}
