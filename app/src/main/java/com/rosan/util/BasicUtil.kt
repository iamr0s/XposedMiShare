package com.rosan.util

import de.robv.android.xposed.XposedBridge

fun log(a: Any?) {
    XposedBridge.log(a.toString())
}