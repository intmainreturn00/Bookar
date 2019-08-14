package com.intmainreturn00.bookar.presentation

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import com.google.ar.sceneform.ux.ArFragment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.TypedValue
import android.view.View


fun generateFilename(): String {
    val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
    return getExternalStoragePublicDirectory(DIRECTORY_PICTURES).absolutePath + separator + "BOOKAR/books" + date + ".jpg"
}


fun saveBitmapToDisk(bitmap: Bitmap, filename: String) {
    val out = File(filename)
    if (!out.parentFile.exists()) {
        out.parentFile.mkdirs()
    }
    try {
        FileOutputStream(filename).use { outputStream ->
            ByteArrayOutputStream().use { outputData ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData)
                outputData.writeTo(outputStream)
                outputStream.flush()
                outputStream.close()
            }
        }
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
}


fun loadBitmapFromView(v: View): Bitmap {
    val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)
    v.layout(v.left, v.top, v.right, v.bottom)
    v.draw(c)
    return b
}


fun compose(sceneform: Bitmap, header: Bitmap, marginStart: Float, marginTop: Float): Bitmap {
    val bmOverlay = Bitmap.createBitmap(sceneform.width, sceneform.height, sceneform.config)
    val canvas = Canvas(bmOverlay)
    canvas.drawBitmap(sceneform, Matrix(), null)
    canvas.drawBitmap(header, marginStart, marginTop, null)
    return bmOverlay
}


fun takePhoto(context: Context, arFragment: ArFragment, header: View) {
    println("@@ takePhoto ${System.currentTimeMillis()}")
    val filename = generateFilename()
    val view = arFragment.arSceneView
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

    val handlerThread = HandlerThread("PixelCopier")
    handlerThread.start()
    PixelCopy.request(view, bitmap, { copyResult ->
        if (copyResult == PixelCopy.SUCCESS) {
            val headerBtm = loadBitmapFromView(header)
            val res = compose(
                bitmap,
                headerBtm,
                dpToPix(context, 20f),
                dpToPix(context, 27f)
            )
            saveBitmapToDisk(res, filename)
            println("@@ takePhoto end ${System.currentTimeMillis()}")
        } else {

        }
        handlerThread.quitSafely()
    }, Handler(handlerThread.looper))
}


fun dpToPix(context: Context, dp: Float): Float =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    )

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}
