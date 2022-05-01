package com.rosan.util

import android.annotation.SuppressLint
import android.content.*
import android.os.IBinder
import com.miui.mishare.*
import java.util.*

class MiShareUtil(
    val context: Context,
    val connection: ServiceConnection,
    val service: IMiShareService
) {
    interface OnBindListener {
        fun onSuccess(miShareUtil: MiShareUtil)

        fun onFailed()
    }

    interface OnSendListener {
        fun onSend(total: Int, current: Int)

        fun onSuccess()

        fun onFailed()
    }

    companion object {
        val componentName = ComponentName(
            "com.miui.mishare.connectivity",
            "com.miui.mishare.connectivity.MiShareService"
        )

        @SuppressLint("QueryPermissionsNeeded")
        fun isAvailable(context: Context): Boolean {
            return context.packageManager.queryIntentServices(
                Intent().setComponent(componentName),
                0
            ).size > 0
        }

        fun bind(context: Context, onBindListener: OnBindListener) {
            if (!isAvailable(context)) {
                onBindListener.onFailed()
                return
            }
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    val service = IMiShareService.Stub.asInterface(binder)
                    if (service != null)
                        onBindListener.onSuccess(MiShareUtil(context, this, service))
                    else
                        onBindListener.onFailed()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }
            if (!context.bindService(
                    Intent().setComponent(componentName),
                    connection,
                    Context.BIND_AUTO_CREATE
                )
            )
                onBindListener.onFailed()
        }
    }

    private var discoverCallback: IMiShareDiscoverCallback? = null

    fun discover(discoverCallback: IMiShareDiscoverCallback?) {
        stopDiscover()
        this.discoverCallback = discoverCallback
        discoverCallback?.let { service.discover(discoverCallback) }
    }

    fun stopDiscover() {
        discoverCallback?.let { service.stopDiscover(discoverCallback) }
    }

    fun send(device: RemoteDevice, clipData: ClipData, onSendListener: OnSendListener): String {
        val taskId = UUID.randomUUID().toString()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.miui.mishare.connectivity.TASK_STATE")
        context.registerReceiver(object : BroadcastReceiver() {
            fun onSuccess() {
                onSendListener.onSuccess()
                unregister()
            }

            fun failed() {
                onSendListener.onFailed()
                unregister()
            }

            fun unregister() {
                context.unregisterReceiver(this)
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                if (context == null) return
                if (intent == null) return
                if ("com.miui.mishare.connectivity.TASK_STATE" != intent.action) return
                /*log("------------")
                intent.extras?.keySet().forEach {
                    log("$it -> (${intent.extras?.get(it)?.javaClass}) -> ${intent.extras?.get(it)}")
                }*/
                val id = intent.getStringExtra("device_id")
                if (taskId != id) return
                val state = intent.getIntExtra("state", -1)
                when (state) {
                    2 -> {
                        val showProgress = intent.getBooleanExtra("showProgress", true)
                        if (showProgress) {
                            failed()
                            return
                        }
                        val total = intent.getIntExtra("total", 1)
                        val current = intent.getIntExtra("current", 1)
                        if (total != 0 && total == current) {
                            onSuccess()
                        } else {
                            onSendListener.onSend(total, current)
                        }
                    }
                    else -> failed()
                }
            }
        }, intentFilter)
        val intent = Intent("com.miui.mishare.action.SEND_TASK")
        intent.component = componentName
        intent.clipData = clipData
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val task = MiShareTask()
        task.clipData = clipData
        task.taskId = taskId
        task.count = clipData.itemCount
        task.device = device
        intent.putExtra("task", task)
        context.startService(
            intent
        )
        return taskId
    }

    fun unbind() {
        context.unbindService(connection)
    }
}