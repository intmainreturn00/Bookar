package com.intmainreturn00.arbooks

import android.content.Context
import android.graphics.Color
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.michaelevans.colorart.library.ColorArt
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random




data class BookModel(val pages: Int?, val cover: String)

data class ARBook(
    val size: Vector3,
    val position: Vector3,
    val rotation: Quaternion,
    val coverUrl: String,
    var coverWidth: Int = 1,
    var coverHeight: Int = 1,
    var coverColor: Int = Color.WHITE
)


suspend fun loadCoverRenderable(context: Context) = ViewRenderable.builder()
    .setView(context, R.layout.cover)
    .build().await()


suspend fun prefetchCovers(context: Context, books: List<ARBook>) {
    withContext(Dispatchers.IO) {
        for (book in books) {
            val btm = downloadImage(context, book.coverUrl)
            btm?.let {
                book.coverWidth = it.width
                book.coverHeight = it.height
                withContext(Dispatchers.Default) {
                    book.coverColor = ColorArt(btm).backgroundColor
                }
            }
        }
    }
}


suspend fun downloadImage(context: Context, url: String) = withContext(Dispatchers.IO) {
    if (url.isNotEmpty()) {
        try {
            val res = GlideApp.with(context).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).submit().get()
            if (res == null || res.width < 10 ) {
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


val offsets = listOf(
    Pair(-1.5f, -1.5f), Pair(-0.5f, -1.5f), Pair(0.5f, -1.5f), Pair(1.5f, -1.5f),
    Pair(-1.5f, -0.5f), Pair(-0.5f, -0.5f), Pair(0.5f, -0.5f), Pair(1.5f, -0.5f),
    Pair(-1.5f, 0.5f), Pair(-0.5f, 0.5f), Pair(0.5f, 0.5f), Pair(1.5f, 0.5f),
    Pair(-1.5f, 1.5f), Pair(-0.5f, 1.5f), Pair(0.5f, 1.5f), Pair(1.5f, 1.5f)
)


fun makeGrid(data: List<BookModel>): MutableList<ARBook> {
    val res = mutableListOf<ARBook>()
    var x: Float
    var z: Float
    var zLayer = 0
    var i = 0

    val elevationMap = MutableList(16) { 0f }

    for (book in data) {

        x = offsets[i].first * (paperbackWidth + 0.03f)
        z = offsets[i].second * (paperbackHeight + 0.03f)

        res.add(
            ARBook(
                makeSize(book.pages),
                Vector3(x, elevationMap[i], z),
                makeAngle(),
                book.cover
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


const val paperbackWidth = 0.1524f // 6''
const val paperbackHeight = 0.2286f // 9''


fun makeDepth(pages: Int?): Float {
    return (pages ?: 600) * 0.00006f + 0.002f
}

fun makeSize(pages: Int?) = Vector3(
    paperbackWidth + (-10..10).random() / 1000f,
    makeDepth(pages),
    paperbackHeight + (-10..10).random() / 1000f
)


fun makeAngle() = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f + (-7..+7).random())


fun makeOpenlibLink(isbn: String) = "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg"


fun makeRandomColor() =
    when (Random.nextInt(10)) {
        1 -> Color.parseColor("#9C27B0")
        2 -> Color.parseColor("#673AB7")
        4 -> Color.parseColor("#3F51B5")
        5 -> Color.parseColor("#607D8B")
        6 -> Color.parseColor("#FF9800")
        else -> Color.WHITE
    }


fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")


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

