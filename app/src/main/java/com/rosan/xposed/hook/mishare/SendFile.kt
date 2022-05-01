package com.rosan.xposed.hook.mishare

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.rosan.xposed.Hook
import com.rosan.xposed.hook.mishare.activity.ShareActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class SendFile(lpparam: XC_LoadPackage.LoadPackageParam) : Hook(lpparam) {
    companion object {
        private fun log(any: Any?) {
            XposedBridge.log("$any")
            Log.e("r0s", "$any")
//        connectivity?.mContext?.let { Toast.makeText(it, "$any", Toast.LENGTH_SHORT).show() }
        }

        var canFinished = false

        @SuppressLint("StaticFieldLeak")
        var shareActivity: ShareActivity? = null
    }

    override fun hooking() {
        XposedHelpers.findAndHookMethod(
            getClass(Activity::class.java),
            "finish",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    val activity = param?.thisObject as Activity? ?: return
                    if (activity::class.java.name != "com.miui.mishare.activity.TransActivity") return
                    if (!canFinished) param?.result = null
                }
            })
        val clazz = getClass("com.miui.mishare.activity.TransActivity") ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    canFinished = false
                }

                override fun afterHookedMethod(param: MethodHookParam?) {
                    val activity = param?.thisObject as Activity? ?: return
                    val bundle = param?.args?.getOrNull(0) as Bundle?
                    canFinished = true
                    if (shareActivity == null) shareActivity = ShareActivity(activity)
                    shareActivity?.activity = activity
                    activity.runOnUiThread { shareActivity?.onCreate(bundle) }
                }
            })
        XposedHelpers.findAndHookMethod(
            getClass(Activity::class.java),
            "onPause",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    val activity = param?.thisObject as Activity? ?: return
                    if (activity::class.java.name != "com.miui.mishare.activity.TransActivity") return
                    activity.runOnUiThread { shareActivity?.onPause() }
                }
            })
        XposedHelpers.findAndHookMethod(
            getClass(Activity::class.java),
            "onResume",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    val activity = param?.thisObject as Activity? ?: return
                    if (activity::class.java.name != "com.miui.mishare.activity.TransActivity") return
                    activity.runOnUiThread { shareActivity?.onResume() }
                }
            })
        XposedHelpers.findAndHookMethod(
            getClass(Activity::class.java),
            "onDestroy",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    val activity = param?.thisObject as Activity? ?: return
                    if (activity::class.java.name != "com.miui.mishare.activity.TransActivity") return
                    activity.runOnUiThread { shareActivity?.onDestroy() }
                }
            })

        /*XposedHelpers.findAndHookMethod(
            getClass("com.miui.mishare.connectivity.MiShareService"),
            "onStartCommand",
            Intent::class.java,
            Int::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    val intent = param?.args?.getOrNull(0) as Intent? ?: return
                    val clipData = intent.clipData ?: return
                    val task = intent.getParcelableExtra("task") as Any? ?: return
                    task::class.java.declaredFields.forEach {
                        it.isAccessible = true
                        if (it.name == "clipData") {
                            val clipData = it.get(task) as ClipData
                            log("----------- ${clipData.description.label}")
                        }
                    }
                }
            })*/
    }
}