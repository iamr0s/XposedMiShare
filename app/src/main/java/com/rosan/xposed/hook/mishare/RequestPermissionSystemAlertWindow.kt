package com.rosan.xposed.hook.mishare

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.rosan.xposed.Hook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class RequestPermissionSystemAlertWindow(lpparam: XC_LoadPackage.LoadPackageParam) : Hook(lpparam) {
    companion object {
        val REQUEST_CODE_MANAGE_OVERLAY_PERMISSION = 10086
    }

    override fun hooking() {
        val clazz = getClass("com.miui.mishare.activity.MiShareSettingsActivity") ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val activity = param?.thisObject as Activity? ?: return
                    val permissions = arrayOf(
                        Permission.SYSTEM_ALERT_WINDOW,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.ACCESS_COARSE_LOCATION,
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.BLUETOOTH_CONNECT,
                        Permission.BLUETOOTH_ADVERTISE,
                        Permission.BLUETOOTH_SCAN
                    )
                    if (XXPermissions.isGranted(activity, permissions)) return
                    Toast.makeText(activity, "请先授予权限", Toast.LENGTH_SHORT).show()
                    XXPermissions.with(activity)
                        .permission(permissions)
                        .request { permissions, all ->
                            if (all) {
                                Toast.makeText(activity, "获取权限成功", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    activity,
                                    "权限获取失败，请手动授权或重新进入应用授权",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                }
            }
        )
        getClass("miui.os.Build")
                ?.getField("IS_INTERNATIONAL_BUILD")
            ?.let {
                it.isAccessible = true
                it.set(null, true)
            }
    }
}