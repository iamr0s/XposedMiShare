package com.rosan.util

import com.miui.mishare.RemoteDevice

fun List<RemoteDevice>.remoteDeviceIndexOf(id: String): Int {
    for ((index, item) in this.withIndex())
        if (id == item.deviceId)
            return index
    return -1
}