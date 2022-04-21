package com.rosan.xposed.hook.mishare

import android.view.Window
import android.view.WindowManager
import com.rosan.xposed.Hook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class FixWindowAlert(lpparam: XC_LoadPackage.LoadPackageParam) : Hook(lpparam) {
    override fun hooking() {
        XposedHelpers.findAndHookMethod(
            getClass(Window::class.java),
            "setType",
            Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    param?.args?.set(0, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                }
            }
        )
    }
}