package re.limus.timas.hook.manager

import re.limus.timas.hook.base.XBridge
import re.limus.timas.hook.generated.ApiItemsRegistry
import re.limus.timas.hook.items.message.MenuMessageRepeat
import re.limus.timas.hook.items.message.core.MenuBuilderApi

/**
 * ApiItems 管理器
 * 负责 ApiItems 的加载
 */
object ApiItemsManager {

    /**
     * 加载所有 ApiItems
     */
    fun loadAllApiItems() {
        ApiItemsRegistry.apiItemsInstances.forEach { apiItem ->
            loadApiItem(apiItem)
        }
    }

    /**
     * 加载单个 ApiItem
     */
    private fun loadApiItem(apiItem: XBridge) {
        if (!apiItem.isLoad) {
            apiItem.startLoad()
        }
    }

    fun processor() {
        //如果是OnMenuBuilder接口的实现类
        if (MenuMessageRepeat.isLoad) {
            MenuBuilderApi.register(MenuMessageRepeat)
        }
    }
}