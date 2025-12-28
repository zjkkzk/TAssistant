package re.limus.timas.hook.manager

import re.limus.timas.hook.base.XBridge
import re.limus.timas.hook.generated.ApiItemsRegistry
import re.limus.timas.hook.generated.HookRegistry
import re.limus.timas.hook.items.message.core.MenuBuilderApi
import re.limus.timas.hook.items.message.core.OnMenuBuilder

/**
 * ApiItems 管理器
 * 负责 ApiItems 的加载
 */
object ApiItemsManager {

    /**
     * 加载所有 ApiItems，并自动注册需要回调的接口
     */
    fun loadAllApiItems() {
        // 自动注册所有实现了 OnMenuBuilder 接口的 Hook
        registerMenuBuilders()

        // 加载所有被 @ApiItems 注解的类
        ApiItemsRegistry.apiItemsInstances.forEach { apiItem ->
            loadApiItem(apiItem)
        }
    }

    /**
     * 扫描并注册所有 OnMenuBuilder 的实现类
     * 这样 PttForward 等类就能通过实现接口被自动识别和注册
     */
    private fun registerMenuBuilders() {
        // 从 KSP 生成的 HookRegistry 中获取所有 Hook 实例
        HookRegistry.hookInstances.forEach { hook ->
            // 判断是否为 OnMenuBuilder 的实例
            if (hook is OnMenuBuilder) {
                // 如果是，则调用 MenuBuilderApi 的 register 方法进行注册
                MenuBuilderApi.register(hook)
            }
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
}
