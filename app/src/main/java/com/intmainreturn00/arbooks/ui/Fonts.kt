package com.intmainreturn00.arbooks.ui

import android.graphics.Typeface
import android.widget.TextView

enum class PodkovaFont(val path: String) {
    EXTRA_BOLD("fonts/Podkova-ExtraBold.ttf"),
    REGULAR("fonts/Podkova-Regular.ttf"),
    MEDIUM("fonts/Podkova-Medium.ttf");

    fun apply(vararg tv: TextView) {
        for (t in tv) {
            t.setCustomFont(this)
        }
    }
}

fun TextView.setCustomFont(font: PodkovaFont) {
    typeface = Typeface.createFromAsset(context?.assets, font.path)
}

