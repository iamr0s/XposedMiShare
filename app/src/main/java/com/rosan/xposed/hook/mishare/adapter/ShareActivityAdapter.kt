package com.rosan.xposed.hook.mishare.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.ArcShape
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.miui.mishare.RemoteDevice
import com.rosan.util.defaultRipple
import com.rosan.util.dp
import com.rosan.util.remoteDeviceIndexOf

class ShareActivityAdapter : BaseAdapter() {
    class Data(
        val device: RemoteDevice,
        val status: Status,
        val total: Int = 0,
        val current: Int = 0
    ) {
        enum class Status {
            Ready,
            SEND_FAILED,
            SENDING,
            SEND_SUCCESS,
        }
    }

    private val datas = arrayListOf<Data>()

    private var onItemClickListener: OnItemClickListener? = null

    fun set(device: RemoteDevice) {
        synchronized(datas) {
            set(Data(device, Data.Status.Ready))
        }
    }

    fun set(data: Data) {
        val device = data.device
        if (device.deviceId == null) return
        remove(device)
        datas.add(data)
        val index = datas.remoteDeviceIndexOf(device.deviceId)
        if (index == -1) return
        notifyDataSetChanged()
    }

    fun remove(data: RemoteDevice) {
        if (data.deviceId == null) return
        remove(data.deviceId)
    }

    fun remove(deviceId: String) {
        synchronized(datas) {
            val index = datas.remoteDeviceIndexOf(deviceId)
            if (index == -1) return
            datas.removeAt(index)
            notifyDataSetChanged()
        }
    }

    override fun notifyDataSetChanged() {
        synchronized(datas) {
            datas.sortBy {
                val rssi = it.device.extras?.getInt(RemoteDevice.KEY_RSSI) ?: Int.MIN_VALUE
                return@sortBy -rssi
            }
            super.notifyDataSetChanged()
        }
    }

    override fun getCount(): Int {
        return datas.size
    }

    override fun getItem(position: Int): Data {
        return datas[position]
    }

    override fun getItemId(position: Int): Long {
        return datas[position].hashCode().toLong()
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val context = parent?.context ?: return null
        val data = getItem(position)
        val device = data.device
        val extras = device.extras ?: return View(context)
        val nickName = extras.getString(RemoteDevice.KEY_NICKNAME)
        val nickNameHasMore = extras.getBoolean(RemoteDevice.KEY_NICKNAME_HAS_MORE)
        val deviceModel = extras.getString(RemoteDevice.KEY_DEVICE_MODEL)
        val deviceManufacture = extras.getString(RemoteDevice.KEY_DEVICE_MANUFACTURE)
        val sgnt = extras.getInt(RemoteDevice.KEY_SUPPORTED_GUIDING_NETWORK_TYPE)
        val isPad = extras.getBoolean("isPad")

        val itemView = FrameLayout(context)
        itemView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        /*when (data.status) {
            Data.Status.SENDING -> itemView.background = ColorDrawable(0xFF4DD0E1.toInt()).apply {
                alpha = data.current / data.total
            }
            Data.Status.SEND_SUCCESS -> itemView.setBackgroundColor(0xFF81C784.toInt())
            Data.Status.SEND_FAILED -> itemView.setBackgroundColor(0xFFE57373.toInt())
            else -> itemView.setBackgroundColor(0x00000000)
        }*/

        val mainView = LinearLayout(context)
        mainView.orientation = LinearLayout.HORIZONTAL
        mainView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        mainView.background = defaultRipple()
        mainView.setOnClickListener {
//            if (data.status != Data.Status.SENDING) {
            onItemClickListener(parent, itemView, position, getItemId(position))
//            }
        }
        itemView.addView(mainView)

        val leftView = FrameLayout(context)
        leftView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(10f.dp.toInt())
        }
        mainView.addView(leftView)

        val imageBackground = FrameLayout(context)
        imageBackground.layoutParams = FrameLayout.LayoutParams(
            40f.dp.toInt(),
            40f.dp.toInt()
        ).apply {
            gravity = Gravity.CENTER /*or Gravity.TOP*/
        }
        imageBackground.background = ShapeDrawable(ArcShape(0f, -360f)).apply {
            paint.color = when (deviceManufacture) {
                "小米" -> 0xFFFF9655.toInt()
                else -> 0xFF808080.toInt()
            }
            paint.style = Paint.Style.FILL
        }

        val image = ImageView(context)
        image.layoutParams = FrameLayout.LayoutParams(
            20f.dp.toInt(),
            20f.dp.toInt()
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
        drawable?.let { DrawableCompat.setTint(it, 0xFFFFFFFF.toInt()) }
        image.setImageDrawable(drawable)
        imageBackground.addView(image)
        leftView.addView(imageBackground)

        val infoView = LinearLayout(context)
        infoView.orientation = LinearLayout.VERTICAL
        infoView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        infoView.gravity = Gravity.LEFT
        infoView.setPadding(10f.dp.toInt())
        mainView.addView(infoView)

        val title = TextView(context)
        title.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        title.setTextColor(0xFF000000.toInt())
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        title.text = "$nickName${if (nickNameHasMore) "..." else ""}"
        infoView.addView(title)

        val text = TextView(context)
        text.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        text.setTextColor(0xFF636363.toInt())
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        text.text = "$deviceManufacture-$deviceModel"
        infoView.addView(text)
        return itemView
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    private fun onItemClickListener(parent: ViewGroup, view: View, position: Int, id: Long) {
        onItemClickListener?.onItemClickListener(parent, view, position, id)
    }

    interface OnItemClickListener {
        fun onItemClickListener(parent: ViewGroup, view: View, position: Int, id: Long)
    }
}
