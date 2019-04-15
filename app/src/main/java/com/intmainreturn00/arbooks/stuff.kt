package com.intmainreturn00.arbooks

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.coroutines.future.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


data class BookModel(val rating: Int?, val pages: Int?, val cover: String)

interface Book

data class ARBookWithCover(
    val size: Vector3,
    val position: Vector3,
    val rotation: Quaternion,
    val coverUrl: String
) : Book


suspend fun downloadBitmap(context: Context, uri: String): Bitmap? = suspendCoroutine { cont ->
    GlideApp.with(context)
        .asBitmap()
        .load(uri)
        .into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                cont.resume(resource)
            }
        })
}

suspend fun loadCoverRenderable(context: Context) = ViewRenderable.builder()
    .setView(context, R.layout.cover)
    .build().await()


fun generateStubBookModels(): List<BookModel> {
    return listOf(
        BookModel(5, 870, "https://images.gr-assets.com/books/1546910265m/2.jpg"),
        BookModel(5, 994, "https://images.gr-assets.com/books/1452624392m/1215032.jpg"),
        BookModel(5, 662, "https://images.gr-assets.com/books/1515589515m/186074.jpg"),
        BookModel(5, 409, "https://images.gr-assets.com/books/1512705866m/30257963.jpg"),
        BookModel(5, 435, "https://images.gr-assets.com/books/1499277281m/5.jpg"),
        BookModel(5, 1125, "https://images.gr-assets.com/books/1327885335m/10664113.jpg"),
        BookModel(5, 277, "https://images.gr-assets.com/books/1398034300m/5107.jpg"),
        BookModel(5, 734, "https://images.gr-assets.com/books/1554006152m/6.jpg"),
        BookModel(5, 255, "https://images.gr-assets.com/books/1421883730m/76171.jpg"),
        BookModel(5, 969, "https://images.gr-assets.com/books/1358254974m/10572.jpg"),
        BookModel(5, 272, "https://images.gr-assets.com/books/1421828465m/12842.jpg"),
        BookModel(5, 218, "https://images.gr-assets.com/books/1357128997m/5759.jpg"),
        BookModel(5, 241, "https://images.gr-assets.com/books/1439197219m/6288.jpg"),
        BookModel(5, 187, "https://images.gr-assets.com/books/1375947566m/5128.jpg"),
        BookModel(5, 436, "https://images.gr-assets.com/books/1517191224m/105.jpg"),
        BookModel(5, 848, "https://images.gr-assets.com/books/1554191954m/13496.jpg"),
        BookModel(5, 482, "https://images.gr-assets.com/books/1405546838m/77566.jpg"),
        BookModel(5, 331, "https://images.gr-assets.com/books/1533872326m/106.jpg"),
        BookModel(5, 454, "https://images.gr-assets.com/books/1327131560m/42432.jpg"),
        BookModel(5, 604, "https://images.gr-assets.com/books/1434908555m/234225.jpg"),
        BookModel(5, 1177, "https://images.gr-assets.com/books/1497931121m/62291.jpg"),
        BookModel(5, 226, "https://images.gr-assets.com/books/1479863624m/1618.jpg")
    )
}

fun fillBooks(books: MutableList<ARBookWithCover>) {

    makeGrid44(generateStubBookModels(), books)
}

fun makeGrid44(data: List<BookModel>, res: MutableList<ARBookWithCover>) {
    val offset = 0.03f
    var x = 0.0f
    var z = 0.0f
    var zLayer = 0
    var i = 0

    val elevationMap = listOf(
        0f, 0f, 0f,
        0f, 0f, 0f,
        0f, 0f, 0f
    )

    for (book in data) {

        x = offsets[i].first * (paperbackWidth + offset)
        z = offsets[i].second * (paperbackHeight + offset)

        res.add(
            ARBookWithCover(
                makeSize(book.pages),
                Vector3(x, 0f, z),
                makeAngle(),
                book.cover
            )
        )

        if (i == 8) {
            break
        }

        i++

    }




}

val offsets = listOf(
    Pair(-1, -1), Pair(0, -1), Pair(1, -1),
    Pair(-1,  0), Pair(0,  0), Pair(1, 0),
    Pair(-1,  1), Pair(0,  1), Pair(1, 1)
)

const val paperbackWidth = 0.1524f // 6''
const val paperbackHeight = 0.2286f // 9''

fun makeDepth(pages: Int?): Float {
    return if (pages != null) {
        pages / 20.0f / 1000f
    } else {
        600 / 20.0f / 1000f
    }
}

fun makeSize(pages: Int?) = Vector3(
    paperbackWidth, //+ (-200..400).random() / 1000f,
    makeDepth(pages),
    paperbackHeight //+ (-200..400).random() / 1000f
)

fun makeAngle() = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f + (-10..+10).random())