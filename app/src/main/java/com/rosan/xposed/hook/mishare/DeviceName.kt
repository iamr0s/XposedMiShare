package com.rosan.xposed.hook.mishare

import com.rosan.data.MiShareData
import com.rosan.xposed.Hook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class DeviceName(lpparam: XC_LoadPackage.LoadPackageParam) : Hook(lpparam) {
    override fun hooking() {
        val systemPropertiesClazz = getClass("miuix.core.util.SystemProperties") ?: return
        XposedHelpers.findAndHookMethod(
            systemPropertiesClazz,
            "get",
            String::class.java,
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    if (param?.args?.getOrNull(0) == "persist.sys.device_name") {
                        param.result = MiShareData.deviceName
                    }
                }
            }
        )
    }
}