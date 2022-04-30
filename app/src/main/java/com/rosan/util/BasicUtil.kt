package com.rosan.util

import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import de.robv.android.xposed.XposedBridge

fun log(a: Any?) {
    XposedBridge.log(a.toString())
    Log.e("r0s", "$a")
}

val Float.px
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PX,
        this,
        Resources.getSystem().displayMetrics
    )

val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Float.sp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )