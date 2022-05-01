package com.rosan.xposed.hook

import android.app.Application
import android.content.Context
import android.content.Intent
import com.rosan.data.MiShareData
import com.rosan.util.log
import com.rosan.xposed.Hook
import com.rosan.xposed.hook.mishare.*
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

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
                    SendFile(lpparam).start()
                    /*XposedHelpers.findAndHookMethod(
                        getClass("com.miui.mishare.connectivity.MiShareService"),
                        "onStartCommand",
                        Intent::class.java,
                        Int::class.java,
                        Int::class.java,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam?) {
                                val intent = param?.args?.getOrNull(0) as Intent? ?: return
                                log("--------------")
                                log(intent)
                                intent.extras?.keySet()?.forEach {
                                    log("key:${it},value: ${intent.extras?.get(it)}")
                                }
//                                val task = intent.getParcelableExtra("task") as Any? ?: return
                            }
                        })*/
                }
            }
        )
    }
}