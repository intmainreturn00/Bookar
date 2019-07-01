package com.intmainreturn00.arbooks

import android.content.Context
import android.graphics.Color
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
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


data class BookModel(
    val id: Int,
    val title: String,
    val authors: List<String>,
    val pages: Int?,
    val rating: Int?,
    val readCount: Int?,
    var coverType: CoverType,
    var cover: String = "",
    var textColor: Int = Color.WHITE,
    var coverColor: Int = Color.WHITE,
    var coverWidth: Int = 1,
    var coverHeight: Int = 1
)


enum class CoverType(val value: Int) {
    COVER(0),
    TEMPLATE1(1),
    TEMPLATE2(2),
    TEMPLATE3(3);

    companion object {
        fun valueOf(value: Int) = values().find { it.value == value } ?: COVER
        fun makeRandomTemplate() = valueOf(Random.nextInt(3) + 1)
    }
}

data class ARBook(
    val title: String,
    val authors: List<String>,
    val size: Vector3,
    val position: Vector3,
    val rotation: Quaternion,
    val coverUrl: String,
    var coverType: CoverType,
    var coverWidth: Int = 1,
    var coverHeight: Int = 1,
    var textColor: Int = Color.WHITE,
    var coverColor: Int = Color.WHITE
)


enum class PLACEMENT { GRID, TOWER }


fun coverBackgroundColor(context: Context, type: CoverType): Int = when (type) {
    CoverType.TEMPLATE1 -> context.getColor(R.color.red)
    CoverType.TEMPLATE2 -> context.getColor(R.color.orange)
    else -> context.getColor(R.color.dark)
}

fun coverTextColor(context: Context, type: CoverType): Int = when (type) {
    CoverType.TEMPLATE1 -> context.getColor(R.color.orange)
    CoverType.TEMPLATE2 -> context.getColor(R.color.dark)
    else -> context.getColor(R.color.orange)
}


fun constructFromReview(review: Review): BookModel = when {
    !review.book.imageUrl.contains("nophoto") ->
        BookModel(
            review.book.id.toInt(), review.book.titleWithoutSeries,
            constructAuthorsTitle(review.book.authors),
            review.book.numPages, review.rating, review.readCount, CoverType.COVER, review.book.imageUrl
        )

    review.book.isbn.isNotEmpty() ->
        BookModel(
            review.book.id.toInt(), review.book.titleWithoutSeries,
            constructAuthorsTitle(review.book.authors),
            review.book.numPages, review.rating, review.readCount, CoverType.COVER, makeOpenlibLink(review.book.isbn)
        )

    else -> BookModel(
        review.book.id.toInt(), review.book.titleWithoutSeries,
        constructAuthorsTitle(review.book.authors),
        review.book.numPages, review.rating, review.readCount, CoverType.COVER, ""
    )
}

fun constructAuthorsTitle(authors: List<Author>): List<String> = authors.take(2).map { it.name }


suspend fun loadCoverRenderable(context: Context, type: CoverType) = when (type) {
    CoverType.COVER -> ViewRenderable.builder().setView(context, R.layout.cover).build().await()
    else -> ViewRenderable.builder().setView(context, R.layout.book_template).build().await()
}


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
                title =  book.title,
                authors = book.authors,
                size = makeSize(book.pages),
                position = Vector3(x, elevationMap[i], z),
                rotation = makeAngle(),
                coverUrl = book.cover,
                coverType = book.coverType,
                coverWidth = book.coverWidth,
                coverHeight = book.coverHeight,
                coverColor = book.coverColor,
                textColor = book.textColor
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


fun makeTower(data: List<BookModel>): MutableList<ARBook> {
    val res = mutableListOf<ARBook>()
    var x: Float
    var z: Float
    var zLayer = 0
    var i = 5

    val elevationMap = MutableList(16) { 0f }

    for (book in data) {

        x = offsets[i].first * (paperbackWidth + 0.03f)
        z = offsets[i].second * (paperbackHeight + 0.03f)

        res.add(
            ARBook(
                title =  book.title,
                authors = book.authors,
                size = makeSize(book.pages),
                position = Vector3(x, elevationMap[i], z),
                rotation = makeAngle(30f),
                coverUrl = book.cover,
                coverType = book.coverType,
                coverWidth = book.coverWidth,
                coverHeight = book.coverHeight,
                coverColor = book.coverColor,
                textColor = book.textColor
            )
        )

        elevationMap[i] += res[res.size - 1].size.y
        if (elevationMap[i] > 2f) {
            return res
        }

        zLayer++

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


fun makeAngle(add: Float = 0f) = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f + add + (-7..+7).random())


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

