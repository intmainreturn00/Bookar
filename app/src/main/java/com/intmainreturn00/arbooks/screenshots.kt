package com.intmainreturn00.arbooks

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
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import android.provider.MediaStore




fun generateFilename(): String {
    val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
    return getExternalStoragePublicDirectory(DIRECTORY_PICTURES).absolutePath + separator + "AR_Books/" + date + "_screenshot.jpg"
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


fun takePhoto(context: Context, arFragment: ArFragment) {
    val filename = generateFilename()
    val view = arFragment.arSceneView

    // Create a bitmap the size of the scene view.
    val bitmap = Bitmap.createBitmap(
        view.width, view.height,
        Bitmap.Config.ARGB_8888
    )

    // Create a handler thread to offload the processing of the image.
    val handlerThread = HandlerThread("PixelCopier")
    handlerThread.start()
    // Make the request to copy.
    PixelCopy.request(view, bitmap, { copyResult ->
        if (copyResult == PixelCopy.SUCCESS) {
            //saveBitmapToDisk(bitmap, filename)
            // true

            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/jpeg"
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap, "Title", null
            )
            val imageUri = Uri.parse(path)
            share.putExtra(Intent.EXTRA_STREAM, imageUri)
            context.startActivity(Intent.createChooser(share, "Select"))

//            snackbar.setAction("Open in Photos") { v ->
//                val photoFile = File(filename)
//
//                val photoURI = FileProvider.getUriForFile(
//                    this@MainActivity,
//                    this@MainActivity.getPackageName() + ".ar.codelab.name.provider",
//                    photoFile
//                )
//                val intent = Intent(Intent.ACTION_VIEW, photoURI)
//                intent.setDataAndType(photoURI, "image/*")
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                startActivity(intent)
//
//            }
//            snackbar.show()
        } else {
//            val toast = Toast.makeText(
//                this@MainActivity,
//                "Failed to copyPixels: $copyResult", Toast.LENGTH_LONG
//            )
//            toast.show()
            // false
        }
        handlerThread.quitSafely()
    }, Handler(handlerThread.looper))
}