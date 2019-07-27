package com.intmainreturn00.arbooks

import android.content.Context
import android.graphics.Color
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.intmainreturn00.arbooks.platform.GlideApp
import com.intmainreturn00.grapi.Author
import com.intmainreturn00.grapi.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.michaelevans.colorart.library.ColorArt
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


suspend fun downloadImage(context: Context, url: String) = withContext(Dispatchers.IO) {
    if (url.isNotEmpty()) {
        try {
            val res = GlideApp.with(context).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).submit().get()
            if (res == null || res.width < 10) {
                null
            } else {
                res
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}

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

