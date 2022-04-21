package com.rosan.xposed.hook.mishare

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.WindowManager
import android.widget.EditText
import com.rosan.data.MiShareData
import com.rosan.xposed.Hook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlin.system.exitProcess

class DeviceNameEdit(lpparam: XC_LoadPackage.LoadPackageParam) : Hook(lpparam) {

    override fun hooking() {
        XposedHelpers.findAndHookMethod(
            getClass(Activity::class.java.name),
            "startActivityForResult",
            Intent::class.java,
            Int::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    val args = param?.args ?: return
                    val activity = param.thisObject as Activity? ?: return
                    val intent = param.args.getOrNull(0) as Intent? ?: return
                    val requestCode = param.args.getOrNull(1) as Int? ?: return
                    val options = param.args.getOrNull(2) as Bundle?
                    if (intent.action != "android.settings.DEVICE_NAME_EDIT") return
                    val editText = EditText(activity)
                    editText.setText(MiShareData.deviceName)
                    AlertDialog.Builder(activity).setTitle("设备名称")
                        .setView(editText)
                        .setPositiveButton("确认并重启") { _, _ ->
                            MiShareData.deviceName = editText.text.toString()
                            XposedBridge.log(MiShareData.deviceName)
                            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?)?.let {
                                it.getRunningServices(Int.MAX_VALUE).forEach {
                                    Process.killProcess(it.pid)
                                }
                            }
                            activity.startActivity(
                                Intent(
                                    activity,
                                    activity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            Process.killProcess(Process.myPid())
                            exitProcess(0)
                            true
                        }
                        .setNegativeButton("取消") { _, _ ->
                            true
                        }
                        .create().apply {
                            window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG)
                        }
                        .show()
                    param.result = null
                }
            })
    }
}