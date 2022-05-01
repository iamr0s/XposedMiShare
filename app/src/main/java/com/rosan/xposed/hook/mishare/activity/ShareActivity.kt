package com.rosan.xposed.hook.mishare.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding
import com.miui.mishare.IMiShareDiscoverCallback
import com.miui.mishare.RemoteDevice
import com.rosan.util.MiShareUtil
import com.rosan.util.defaultRipple
import com.rosan.util.dp
import com.rosan.util.log
import com.rosan.xposed.hook.mishare.adapter.ShareActivityAdapter

class ShareActivity(var activity: Activity) {
    val adapter = ShareActivityAdapter().apply {
        setOnItemClickListener(object : ShareActivityAdapter.OnItemClickListener {
            override fun onItemClickListener(
                parent: ViewGroup,
                view: View,
                position: Int,
                id: Long
            ) {
                val data = getItem(position)

                var clipData = activity.intent.clipData
                if (clipData == null) {
                    clipData = ClipData.newUri(activity.contentResolver, "", activity.intent.data)
                }
                if (clipData == null) {
                    return
                }
                miShareUtil?.send(data.device, clipData, object : MiShareUtil.OnSendListener {
                    override fun onSend(total: Int, current: Int) {
                    }

                    override fun onSuccess() {
                    }

                    override fun onFailed() {
                    }
                })
            }
        })
    }

    var dialog: AlertDialog? = null

    var emptyTitleView: TextView? = null

    var miShareUtil: MiShareUtil? = null

    fun onCreate(savedInstanceState: Bundle?) {
        val rootView = FrameLayout(activity)
        rootView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        val emptyView = getEmptyView()
        emptyView.visibility = View.GONE
        rootView.addView(emptyView)

        val listView = GridView(activity)
        listView.layoutParams = AbsListView.LayoutParams(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.MATCH_PARENT
        )
        listView.numColumns = 1
        listView.selector = ColorDrawable(0x00000000)
        listView.adapter = adapter
        listView.emptyView = emptyView
        rootView.addView(listView)

        dialog = AlertDialog.Builder(activity)
            .setTitle("选择设备")
            .setView(rootView)
            .setPositiveButton("取消") { _, _ -> }
            .setOnCancelListener { activity.finish() }
            .setOnDismissListener { activity.finish() }
            .show()
    }

    private fun getEmptyView(): View {
        val rootView = LinearLayout(activity)
        rootView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        rootView.orientation = LinearLayout.VERTICAL
        rootView.gravity = Gravity.CENTER
        rootView.setPadding(10f.dp.toInt())
        rootView.background = defaultRipple()
        rootView.setOnClickListener { activity.startActivity(Intent("com.miui.mishare.action.MiShareSettings")) }

        val titleView = TextView(activity)
        titleView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        titleView.gravity = Gravity.CENTER
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        emptyTitleView = titleView
        setEmptyTitle("正在搜索附近的设备...", 0xFF000000.toInt())
        rootView.addView(titleView)

        val textView = TextView(activity)
        textView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        textView.gravity = Gravity.CENTER
        textView.setTextColor(0xFF808080.toInt())
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        textView.text = "点击进入设置页"
        rootView.addView(textView)

        return rootView
    }

    private fun setEmptyTitle(charSequence: CharSequence?, color: Int) {
        emptyTitleView?.text = charSequence
        emptyTitleView?.setTextColor(color)
    }

    fun onResume() {
        MiShareUtil.bind(activity, object : MiShareUtil.OnBindListener {
            override fun onSuccess(miShareUtil: MiShareUtil) {
                this@ShareActivity.miShareUtil = miShareUtil
                setEmptyTitle("正在搜索附近的设备...", 0xFF000000.toInt())
                activity.runOnUiThread { adapter.clear() }
                miShareUtil.discover(object : IMiShareDiscoverCallback.Stub() {
                    override fun onDeviceLost(deviceId: String?) {
                        if (deviceId == null) return
                        activity.runOnUiThread { adapter.remove(deviceId) }
                    }

                    override fun onDeviceUpdated(device: RemoteDevice?) {
                        if (device == null) return
                        activity.runOnUiThread { adapter.set(device) }
                    }
                })
            }

            override fun onFailed() {
                setEmptyTitle("搜索附近的设备失败", 0xFFB71C1C.toInt())
            }
        })
    }

    fun onPause() {
        miShareUtil?.stopDiscover()
        miShareUtil?.unbind()
    }

    fun onDestroy() {
        if (dialog?.isShowing == true) dialog?.dismiss()
    }
}