package com.rosan.data

import android.os.Bundle
import android.os.Parcel
import com.miui.mishare.RemoteDevice

class RemoteDeviceStub(p0: Parcel?) : RemoteDevice(p0) {
    constructor(deviceId: String, extras: Bundle) : this(Parcel.obtain().apply {
        writeString(deviceId)
        writeBundle(extras)
    })

    constructor(deviceId: String) : this(deviceId, Bundle())
}