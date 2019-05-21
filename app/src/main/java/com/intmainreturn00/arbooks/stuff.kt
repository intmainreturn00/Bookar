package com.intmainreturn00.arbooks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.michaelevans.colorart.library.ColorArt
import kotlin.random.Random


data class BookModel(val pages: Int?, val cover: String)

data class ARBook(
    val size: Vector3,
    val position: Vector3,
    val rotation: Quaternion,
    val coverUrl: String,
    var width: Int = 0,
    var height: Int = 0,
    var coverColor: Int = Color.WHITE
)


suspend fun loadCoverRenderable(context: Context) = ViewRenderable.builder()
    .setView(context, R.layout.cover)
    .build().await()


suspend fun prefetchCovers(context: Context, books: List<ARBook>, update: (b: Bitmap?, color: Int) -> Unit) {
    println("@prefetching covers")
    withContext(Dispatchers.IO) {
        for (book in books) {
            val btm = downloadCover(context, book)
            btm?.let {
                book.width = it.width
                book.height = it.height
                withContext(Dispatchers.Default) {
                    book.coverColor = ColorArt(btm).backgroundColor
                }
            }
            withContext(Dispatchers.Main) {
                update(btm, book.coverColor)
            }
        }
    }
}


suspend fun downloadCover(context: Context, book: ARBook) = withContext(Dispatchers.IO) {
    if (book.coverUrl.isEmpty()) {
        null
    } else {
        try {
            GlideApp.with(context)
                .asBitmap()
                .load(book.coverUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                //.skipMemoryCache(true)
                .placeholder(R.drawable.dcover2)
                .error(R.drawable.dcover2)
                .submit().get()
        } catch (e: java.lang.Exception) {
            println("@ ${book.coverUrl}")
            null
        }
    }
}


fun makeGrid(data: List<BookModel>): MutableList<ARBook> {
    val res = mutableListOf<ARBook>()
    val offset = 0.03f
    var x: Float
    var z: Float
    var zLayer = 0
    var i = 0

    val elevationMap = mutableListOf(
        0f, 0f, 0f, 0f,
        0f, 0f, 0f, 0f,
        0f, 0f, 0f, 0f,
        0f, 0f, 0f, 0f
    )

    for (book in data) {

        x = offsets[i].first * (paperbackWidth + offset)
        z = offsets[i].second * (paperbackHeight + offset)

        res.add(
            ARBook(
                makeSize(book.pages),
                Vector3(x, elevationMap[i], z),
                makeAngle(),
                book.cover,
                1, 1
            )
        )

        elevationMap[i] += res[res.size - 1].size.y

        if (i == 15) {
            zLayer++
            i = 0
        } else {
            i++
        }
    }

    return res
}


val offsets = listOf(
    Pair(-1.5f, -1.5f), Pair(-0.5f, -1.5f), Pair(0.5f, -1.5f), Pair(1.5f, -1.5f),
    Pair(-1.5f, -0.5f), Pair(-0.5f, -0.5f), Pair(0.5f, -0.5f), Pair(1.5f, -0.5f),
//                                         .
    Pair(-1.5f, 0.5f), Pair(-0.5f, 0.5f), Pair(0.5f, 0.5f), Pair(1.5f, 0.5f),
    Pair(-1.5f, 1.5f), Pair(-0.5f, 1.5f), Pair(0.5f, 1.5f), Pair(1.5f, 1.5f)
)


const val paperbackWidth = 0.1524f // 6''
const val paperbackHeight = 0.2286f // 9''


fun makeDepth(pages: Int?): Float {
    return if (pages != null) {
        pages / 17.0f / 1000f
    } else {
        600 / 17.0f / 1000f
    }
}


fun makeSize(pages: Int?) = Vector3(
    paperbackWidth + (-10..10).random() / 1000f,
    makeDepth(pages),
    paperbackHeight + (-10..10).random() / 1000f
)


fun makeAngle() = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f + (-7..+7).random())


fun makeOpenlibLink(isbn: String) = "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg"


// https://materialuicolors.co/
fun makeRandomColor() =
    when (Random.nextInt(20)) {
        1 -> Color.parseColor("#673AB7")
        2 -> Color.parseColor("#3F51B5")
        4 -> Color.parseColor("#2196F3")
        5 -> Color.parseColor("#03A9F4")
        6 -> Color.parseColor("#00BCD4")
        7 -> Color.parseColor("#009688")
        8 -> Color.parseColor("#4CAF50")
        9 -> Color.parseColor("#8BC34A")
        10 -> Color.parseColor("#CDDC39")
        11 -> Color.parseColor("#FFEB3B")
        12 -> Color.parseColor("#FFC107")
        13 -> Color.parseColor("#FF9800")
        14 -> Color.parseColor("#FF5722")
        15 -> Color.parseColor("#9E9E9E")
        16 -> Color.parseColor("#607D8B")
        else -> Color.WHITE
    }


fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")