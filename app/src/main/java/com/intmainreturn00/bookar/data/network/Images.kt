package com.intmainreturn00.bookar.data.network

import android.content.Context
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.intmainreturn00.bookar.platform.GlideApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
    Download image from given url.
    Filter images less than 10 px width (empty placeholders)
 */
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