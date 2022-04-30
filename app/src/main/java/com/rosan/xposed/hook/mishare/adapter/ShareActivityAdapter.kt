package com.rosan.xposed.hook.mishare.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.miui.mishare.RemoteDevice
import com.rosan.data.RemoteDeviceStub
import com.rosan.util.dp
import com.rosan.util.log
import com.rosan.util.remoteDeviceIndexOf
import kotlin.math.acos

class ShareActivityAdapter : BaseAdapter() {
    private val datas = arrayListOf<RemoteDevice>()

    fun add(data: RemoteDevice) {
        if (data.deviceId == null) return
        remove(data)
        datas.add(data)
        val index = datas.remoteDeviceIndexOf(data.deviceId)
        if (index == -1) return
        notifyDataSetChanged()
    }

    fun remove(data: RemoteDevice) {
        if (data.deviceId == null) return
        remove(data.deviceId)
    }

    fun remove(deviceId: String) {
        val index = datas.remoteDeviceIndexOf(deviceId)
        if (index == -1) return
        datas.removeAt(index)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return datas.size
    }

    override fun getItem(position: Int): Any {
        return datas[position]
    }

    override fun getItemId(position: Int): Long {
        return datas[position].hashCode().toLong()
    }

    @SuppressLint("ResourceType")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val context = parent?.context ?: return null
        val device = datas[position]
        val extras = device.extras ?: return View(context)
        val nickName = extras.getString(RemoteDevice.KEY_NICKNAME)
        val nickNameHasMore = extras.getBoolean(RemoteDevice.KEY_NICKNAME_HAS_MORE)
        val deviceModel = extras.getString(RemoteDevice.KEY_DEVICE_MODEL)
        val deviceManufacture = extras.getString(RemoteDevice.KEY_DEVICE_MANUFACTURE)
        val sgnt = extras.getInt(RemoteDevice.KEY_SUPPORTED_GUIDING_NETWORK_TYPE)
        val isPad = extras.getBoolean("isPad")
        log(extras)

        val view = LinearLayout(context)
        view.orientation = LinearLayout.HORIZONTAL
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val imageBackground = FrameLayout(context)
        imageBackground.layoutParams = FrameLayout.LayoutParams(
            60f.dp.toInt(),
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        val image = ImageView(context)
        image.layoutParams = FrameLayout.LayoutParams(
            30f.dp.toInt(),
            30f.dp.toInt()
        ).apply {
            gravity = Gravity.CENTER
        }

        /**
         * 0x7F080415 pad
         * 0x7F080416 pc
         * 0x7F080417 phone
         * */
        val drawable =
            when {
                isPad -> ContextCompat.getDrawable(context, 0x7F080415.toInt())
                sgnt == 2 -> ContextCompat.getDrawable(context, 0x7F080416.toInt())
                else -> ContextCompat.getDrawable(context, 0x7F080417.toInt())
            }
        drawable?.let { DrawableCompat.setTint(it, 0xFF000000.toInt()) }
        image.setImageDrawable(drawable)
        imageBackground.addView(image)
        view.addView(imageBackground)

        val infoView = LinearLayout(context)
        infoView.orientation = LinearLayout.VERTICAL
        infoView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        infoView.setPadding(10f.dp.toInt())
        val title = TextView(context)
        title.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        title.setTextColor(0xFF000000.toInt())
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        title.text = "$nickName${if (nickNameHasMore) "..." else ""}"
        infoView.addView(title)

        val text = TextView(context)
        text.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        text.setTextColor(0xFF636363.toInt())
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        text.text = "$deviceManufacture-$deviceModel"
        infoView.addView(text)

        view.addView(infoView)
        return view
    }

    fun onItemClickListener(parent: AdapterView<*>, view: View, position1: Int, id: Long) {
        Toast.makeText(parent.context, "待实现", Toast.LENGTH_SHORT).show()
    }
}
