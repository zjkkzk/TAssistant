package re.limus.timas.hook.base

import android.app.Application
import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import re.limus.timas.hook.utils.XLog
import top.sacz.xphelper.ext.toClass
import top.sacz.xphelper.reflect.MethodUtils

abstract class PluginHook : SwitchHook() {

    abstract val pluginID: String

    private var pluginClassLoader: ClassLoader? = null
    private var hookPending = false
    @Volatile
    private var hookDone = false

    override fun onHook(ctx: Context, loader: ClassLoader) {
        if (pluginID.isEmpty()) {
            XLog.e("pluginID is empty for ${this::class.simpleName}")
            return
        }

        // 尝试直接加载，如果失败则延迟到 Application.onCreate 后
        try {
            initPluginProxy()
            pluginClassLoader = getOrCreateClassLoader(ctx, pluginID)
            if (pluginClassLoader != null) {
                onPluginHook(ctx, pluginClassLoader!!)
                return
            }
        } catch (t: Throwable) {
            XLog.d("Direct load failed, will retry after Application.onCreate", t)
        }

        // 延迟加载：Hook Application.onCreate
        if (!hookPending) {
            hookPending = true
            hookApplicationOnCreate()
        }
    }

    private fun hookApplicationOnCreate() {
        try {
            val onCreateMethod = Application::class.java.getDeclaredMethod("onCreate")
            XposedBridge.hookMethod(onCreateMethod, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (hookDone) return
                    val app = param.thisObject as Application
                    if (app.packageName != "com.tencent.tim") return

                    try {
                        initPluginProxy()
                        pluginClassLoader = getOrCreateClassLoader(app, pluginID)
                        if (pluginClassLoader != null) {
                            hookDone = true
                            onPluginHook(app, pluginClassLoader!!)
                        }
                    } catch (e: Throwable) {
                        XLog.e("Failed to hook plugin after onCreate: $pluginID", e)
                    }
                }
            })
        } catch (e: Throwable) {
            XLog.e("Failed to hook Application.onCreate", e)
        }
    }

    private fun initPluginProxy() {
        try {
            val proxyClass = "com.tencent.mobileqq.pluginsdk.IPluginAdapterProxy".toClass()
            val getProxyMethod = MethodUtils.create(proxyClass)
                .methodName("getProxy")
                .returnType(proxyClass)
                .first()
            getProxyMethod.isAccessible = true

            if (getProxyMethod.invoke(null) == null) {
                val setProxyMethod = MethodUtils.create(proxyClass)
                    .methodName("setProxy")
                    .params("com.tencent.mobileqq.pluginsdk.IPluginAdapter".toClass())
                    .first()
                setProxyMethod.isAccessible = true

                val adapterImpl = listOf(
                    "cooperation.plugin.c",
                    "cooperation.plugin.PluginAdapterImpl",
                    "bghq",
                    "bfdk",
                    "avgk",
                    "avel"
                ).firstNotNullOfOrNull { className ->
                    runCatching { className.toClass() }.getOrNull()
                }?.getDeclaredConstructor()?.apply { isAccessible = true }?.newInstance()

                if (adapterImpl != null) {
                    setProxyMethod.invoke(null, adapterImpl)
                }
            }
        } catch (_: Throwable) {
            // 忽略，可能已经初始化
        }
    }

    private fun getOrCreateClassLoader(ctx: Context, pluginID: String): ClassLoader? {
        return try {
            val method = MethodUtils.create("com.tencent.mobileqq.pluginsdk.PluginStatic")
                .methodName("getOrCreateClassLoader")
                .params(Context::class.java, String::class.java)
                .returnType(ClassLoader::class.java)
                .first()
            method.isAccessible = true
            method.invoke(null, ctx, pluginID) as? ClassLoader
        } catch (_: Throwable) {
            null
        }
    }

    abstract fun onPluginHook(ctx: Context, pluginLoader: ClassLoader)
}
