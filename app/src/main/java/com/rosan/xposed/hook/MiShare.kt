package com.rosan.xposed.hook

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.widget.Toast
import com.rosan.data.MiShareData
import com.rosan.util.log
import com.rosan.xposed.Hook
import com.rosan.xposed.hook.mishare.DeviceName
import com.rosan.xposed.hook.mishare.DeviceNameEdit
import com.rosan.xposed.hook.mishare.FixWindowAlert
import com.rosan.xposed.hook.mishare.RequestPermissionSystemAlertWindow
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlin.math.log

class MiShare(lpparam: XC_LoadPackage.LoadPackageParam) : Hook(lpparam) {
    companion object {
        const val PACKAGE_NAME = "com.miui.mishare.connectivity"
    }

    override fun beforeHook(): Boolean {
        return isPackageName(PACKAGE_NAME)
    }

    override fun hooking() {
        XposedHelpers.findAndHookMethod(
            Application::class.java,
            "attach",
            Context::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    val context = param?.args?.getOrNull(0) as Context? ?: return
                    MiShareData.init(context)
                    DeviceName(lpparam).start()
                    DeviceNameEdit(lpparam).start()
                    FixWindowAlert(lpparam).start()
                    RequestPermissionSystemAlertWindow(lpparam).start()
                }
            }
        )
    }
}