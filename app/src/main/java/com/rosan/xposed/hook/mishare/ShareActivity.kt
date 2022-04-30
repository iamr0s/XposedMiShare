package com.rosan.xposed.hook.mishare

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.AbsListView
import android.widget.GridView
import com.miui.mishare.*
import com.miui.mishare.app.connect.MiShareGalleryConnectivity
import com.rosan.util.px
import com.rosan.xposed.Hook
import com.rosan.xposed.hook.mishare.adapter.ShareActivityAdapter
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class ShareActivity(lpparam: XC_LoadPackage.LoadPackageParam) : Hook(lpparam) {
    companion object {
        private fun log(any: Any?) {
            XposedBridge.log("$any")
            Log.e("r0s", "$any")
//        connectivity?.mContext?.let { Toast.makeText(it, "$any", Toast.LENGTH_SHORT).show() }
        }

        var canFinished = false

        var connectivity: MiShareGalleryConnectivity? = null

        val taskStateListener: IMiShareTaskStateListener =
            object : IMiShareTaskStateListener.Stub() {
                override fun onTaskIdChanged(p0: MiShareTask?) {
                    log("onTaskIdChanged $p0")
                }

                override fun onTaskStateChanged(p0: String?, p1: Int) {
                    log("onTaskStateChanged $p0 $p1")
                }
            }

        val discoverCallback: IMiShareDiscoverCallback = object : IMiShareDiscoverCallback.Stub() {
            override fun onDeviceLost(p0: String?) {
                discoverCallbackMap.forEach {
                    it.value.onDeviceLost(p0)
                }
            }

            override fun onDeviceUpdated(p0: RemoteDevice?) {
                discoverCallbackMap.forEach {
                    it.value.onDeviceUpdated(p0)
                }
            }
        }

        val stateListener: IMiShareStateListener = object : IMiShareStateListener.Stub() {
            override fun onStateChanged(p0: Int) {
                log("onStateChanged $p0")
            }
        }

        val discoverCallbackMap = HashMap<Int, IMiShareDiscoverCallback>()
    }

    override fun hooking() {
        canFinished = false
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
        XposedHelpers.findAndHookMethod(
            getClass(Activity::class.java),
            "onDestroy",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    val activity = param?.thisObject as Activity? ?: return
                    if (activity::class.java.name != "com.miui.mishare.activity.TransActivity") return
                    if (connectivity?.mIsBound != true) return
                    if (connectivity?.mService == null) return
                    connectivity?.unregisterTaskStateListener(taskStateListener)
                    connectivity?.stopDiscover(discoverCallback)
                    connectivity?.unregisterStateListener(stateListener)
                    connectivity?.unbind()
                    log("解绑")
                }
            })
        val clazz = getClass("com.miui.mishare.activity.TransActivity") ?: return
        XposedHelpers.findAndHookMethod(
            clazz,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val activity = param?.thisObject as Activity? ?: return
                    canFinished = true
                    if (connectivity == null) connectivity = MiShareGalleryConnectivity(activity)
                    val connectivity = connectivity!!
                    if (MiShareGalleryConnectivity.isAvailable(activity) && !connectivity.checkServiceBound()) connectivity.bind {
                        bindSuccess(activity, connectivity)
                    }
                    if (!connectivity.mIsBound) bindFailed(activity)
                }
            })
    }

    private fun bindFailed(activity: Activity) {
        log("请先开启小米快传")
        activity.finish()
    }

    private fun bindSuccess(activity: Activity, connectivity: MiShareGalleryConnectivity) {
        connectivity.registerStateListener(stateListener)
        connectivity.startDiscover(discoverCallback)
        connectivity.registerTaskStateListener(taskStateListener)
        val adapter = ShareActivityAdapter()
        val view = GridView(activity)
        view.layoutParams = AbsListView.LayoutParams(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.MATCH_PARENT
        )
        view.adapter = adapter
        view.isPressed = true
        view.verticalSpacing = 1f.px.toInt()
        view.setOnItemClickListener { parent, view, position, id ->
            adapter.onItemClickListener(parent, view, position, id)
        }
        AlertDialog.Builder(activity)
            .setTitle("选择设备")
            .setView(view)
            .setPositiveButton("取消") { _, _ ->
                discoverCallbackMap.remove(activity.hashCode())
                activity.finish()
            }
            .show()
        discoverCallbackMap[activity.hashCode()] = object : IMiShareDiscoverCallback.Stub() {
            override fun onDeviceLost(deviceId: String?) {
                if (deviceId == null) return
                activity.runOnUiThread { adapter.remove(deviceId) }
            }

            override fun onDeviceUpdated(device: RemoteDevice?) {
                if (device == null) return
                activity.runOnUiThread { adapter.add(device) }
            }
        }
    }

    private fun getUris(activity: Activity): Array<out Parcelable?>? {
        return when (activity.intent.action) {
            Intent.ACTION_SEND -> arrayOf(activity.intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
            Intent.ACTION_SEND_MULTIPLE -> activity.intent.getParcelableArrayExtra(Intent.EXTRA_STREAM)
            else -> null
        }
    }
}