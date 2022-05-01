package com.rosan.util

import com.miui.mishare.RemoteDevice
import com.rosan.xposed.hook.mishare.adapter.ShareActivityAdapter

fun List<ShareActivityAdapter.Data>.remoteDeviceIndexOf(id: String): Int {
    for ((index, item) in this.withIndex())
        if (id == item.device.deviceId)
            return index
    return -1
}