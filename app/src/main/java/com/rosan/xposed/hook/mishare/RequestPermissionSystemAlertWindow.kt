package com.rosan.xposed.hook.mishare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.rosan.xposed.Hook
import com.rosan.xposed.hook.MiShare
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
                    if (Settings.canDrawOverlays(activity)) return
                    Toast.makeText(activity, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show()
                    XXPermissions.with(activity)
                        .permission(Permission.SYSTEM_ALERT_WINDOW)
                        .request { permissions, all ->
                            if (all) {
                                Toast.makeText(activity, "获取悬浮窗权限成功", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    activity,
                                    "悬浮窗权限获取失败，请手动授权或重新进入应用授权",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    /*activity.startActivityForResult(
                            Intent()
                                .setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                .setData(Uri.fromParts("package", MiShare.PACKAGE_NAME, null)),
                            REQUEST_CODE_MANAGE_OVERLAY_PERMISSION
                        )*/
                }
            }
        )
    }
}