package com.rosan.util

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable

fun defaultRipple(): RippleDrawable {
    return RippleDrawable(
        ColorStateList.valueOf(0x21212721.toInt()),
        null,
        ColorDrawable(0xFFFFFFFF.toInt())
    )
}