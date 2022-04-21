package com.rosan.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.rosan.xposed.mishare.BuildConfig
import de.robv.android.xposed.XSharedPreferences

class MiShareData : BasicData {
    companion object {
        lateinit var context: Context

        lateinit var sharedPreferences: SharedPreferences

        fun init(context: Context) {
            this.context = context
            sharedPreferences = context.getSharedPreferences("xposed_mishare", Context.MODE_PRIVATE)
        }

        var deviceName: String
            get() = sharedPreferences.getString("device_name", null) ?: Settings.Global.getString(
                context.contentResolver,
                Settings.Global.DEVICE_NAME
            ) ?: "r0s MiShare"
            set(value) {
                sharedPreferences.edit().putString("device_name", value).commit()
            }
    }
}