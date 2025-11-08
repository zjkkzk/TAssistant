package io.live.timas.api

import android.content.Context
import io.live.timas.hook.base.XBridge
import top.sacz.xphelper.ext.toClass
import top.sacz.xphelper.reflect.ConstructorUtils
import top.sacz.xphelper.reflect.FieldUtils

class TIMContactUpdateListener : XBridge() {
    companion object {
        private var currentAIOContact: Any? = null

        @JvmStatic
        fun getCurrentAIOContact(): Any {
            return currentAIOContact!!
        }

    }

    override fun onHook(ctx: Context, loader: ClassLoader) {

        val aioContextImpl: Class<*> = "com.tencent.aio.runtime.AIOContextImpl".toClass()
        ConstructorUtils.create(aioContextImpl)
            .paramTypes(
                "com.tencent.aio.main.fragment.ChatFragment".toClass(),
                "com.tencent.aio.data.AIOParam".toClass(),
                "androidx.lifecycle.LifecycleOwner".toClass(),
                "kotlin.jvm.functions.Function0".toClass()

            ).first()
            .hookBefore {
                val aioParam = args[1]
                val aioSession: Any = FieldUtils.create(aioParam)
                    .fieldType("com.tencent.aio.data.AIOSession".toClass())
                    .firstValue(aioParam)
                val aioContact: Any = FieldUtils.create(aioSession)
                    .fieldType("com.tencent.aio.data.AIOContact".toClass())
                    .firstValue(aioSession)
                currentAIOContact = aioContact
            }
    }
}