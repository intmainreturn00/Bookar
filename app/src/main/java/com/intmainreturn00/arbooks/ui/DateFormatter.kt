package com.intmainreturn00.arbooks.ui

import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*

fun formatProfileAge(joined: String): Pair<String, String> {
    val dateFormat = SimpleDateFormat("MM/yyyy", Locale.US)
    try {
        val date = dateFormat.parse(joined)
        val dateString = PrettyTime().format(date).replace(" ago", "")
        val res = dateString.split(" ")

        return Pair(res[0], res[1])

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Pair("", "")
}