package com.intmainreturn00.arbooks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.net.HttpURLConnection
import java.net.URL


data class BookModel(val pages: Int?, val cover: String)

interface Book

data class ARBookWithCover(
    val size: Vector3,
    val position: Vector3,
    val rotation: Quaternion,
    val coverUrl: String
) : Book


fun parseDate(date: String): Date {
    if (date.isEmpty()) {
        return Date(1997, 1, 1)
    }
    val format = SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US)
    try {
        val date = format.parse(date)
        return date
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return Date(1997, 1, 1)
}

suspend fun downloadBitmap(context: Context, uri: String): Bitmap? = suspendCoroutine { cont ->
    GlideApp.with(context)
        .asBitmap()
        .load(uri)
        .into(object : SimpleTarget<Bitmap>(266, 432) {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                cont.resume(resource)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                val bmp = BitmapFactory.decodeResource(context.resources,
                    R.drawable.dcover)
                cont.resume(bmp)
            }
        })
}

suspend fun downloadBitmap2(context: Context, uri: String): Bitmap? =
    withContext(Dispatchers.IO) {
        var firstConn: HttpURLConnection? = null
        try {
            val btm: Bitmap
            val url = URL(uri)
            firstConn = url.openConnection() as HttpURLConnection
            firstConn.instanceFollowRedirects = false
            val redirectURLString = firstConn.getHeaderField("Location")
            if (redirectURLString == null) {
                btm = BitmapFactory.decodeStream(firstConn.inputStream)
            } else {
                val redirectURL = URL(firstConn.getHeaderField("Location"))
                val redirectConn = redirectURL.openConnection()
                btm = BitmapFactory.decodeStream(redirectConn.getInputStream())
            }

            btm
        } catch (e : Exception) {
            e.printStackTrace()
            BitmapFactory.decodeResource(context.resources,
                R.drawable.dcover)
        } finally {
            firstConn?.disconnect()
            BitmapFactory.decodeResource(context.resources,
                R.drawable.dcover)
        }
    }


suspend fun loadCoverRenderable(context: Context) = ViewRenderable.builder()
    .setView(context, R.layout.cover)
    .build().await()


fun generateStubBookModels(): List<BookModel> {
    return listOf(
        BookModel(870, "https://images.gr-assets.com/books/1546910265m/2.jpg"),
        BookModel(994, "https://images.gr-assets.com/books/1452624392m/1215032.jpg"),
        BookModel(662, "https://images.gr-assets.com/books/1515589515m/186074.jpg"),
        BookModel(434, "http://covers.openlibrary.org/b/isbn/1597800449-L.jpg"),
        BookModel(409, "https://images.gr-assets.com/books/1512705866m/30257963.jpg"),
        BookModel(435, "https://images.gr-assets.com/books/1499277281m/5.jpg"),
        BookModel(1125, "https://images.gr-assets.com/books/1327885335m/10664113.jpg"),
        BookModel(277, "https://images.gr-assets.com/books/1398034300m/5107.jpg"),
        BookModel(734, "https://images.gr-assets.com/books/1554006152m/6.jpg"),
        BookModel(255, "https://images.gr-assets.com/books/1421883730m/76171.jpg"),
        BookModel(969, "https://images.gr-assets.com/books/1358254974m/10572.jpg"),
        BookModel(272, "https://images.gr-assets.com/books/1421828465m/12842.jpg"),
        BookModel(243, "http://covers.openlibrary.org/b/isbn/0061020680-L.jpg"),
        BookModel(218, "https://images.gr-assets.com/books/1357128997m/5759.jpg"),
        BookModel(241, "https://images.gr-assets.com/books/1439197219m/6288.jpg"),
        BookModel(187, "https://images.gr-assets.com/books/1375947566m/5128.jpg"),
        BookModel(1040, "http://covers.openlibrary.org/b/isbn/0201700735-L.jpg"),
        BookModel(436, "https://images.gr-assets.com/books/1517191224m/105.jpg"),
        BookModel(848, "https://images.gr-assets.com/books/1554191954m/13496.jpg"),
        BookModel(482, "https://images.gr-assets.com/books/1405546838m/77566.jpg"),
        BookModel(331, "https://images.gr-assets.com/books/1533872326m/106.jpg"),
        BookModel(408, "http://covers.openlibrary.org/b/isbn/0441104029-L.jpg"),
        BookModel(471, "http://covers.openlibrary.org/b/isbn/0441328008-L.jpg"),
        BookModel(null, "http://covers.openlibrary.org/b/isbn/0425099628-L.jpg"),
        BookModel(454, "https://images.gr-assets.com/books/1327131560m/42432.jpg"),
        BookModel(604, "https://images.gr-assets.com/books/1434908555m/234225.jpg"),
        BookModel(1177, "https://images.gr-assets.com/books/1497931121m/62291.jpg"),
        BookModel(226, "https://images.gr-assets.com/books/1479863624m/1618.jpg"),
        BookModel(544, "http://covers.openlibrary.org/b/isbn/5170215452-L.jpg"),
        BookModel(1061, "https://images.gr-assets.com/books/1429538615m/13497.jpg"),
        BookModel(341, "https://images.gr-assets.com/books/1474169725m/15881.jpg"),
        BookModel(271, "https://images.gr-assets.com/books/1548535751m/22328.jpg"),
        BookModel(2007, "https://images.gr-assets.com/books/1293582551m/10016013.jpg"),
        BookModel(182, "https://images.gr-assets.com/books/1427731744m/17125.jpg"),
        BookModel(453, "https://images.gr-assets.com/books/1463157317m/168668.jpg"),
        BookModel(320, "http://covers.openlibrary.org/b/isbn/0765342537-L.jpg"),
        BookModel(311, "https://images.gr-assets.com/books/1554312314m/18373.jpg"),
        BookModel(256, "https://images.gr-assets.com/books/1466165860m/30637548.jpg"),
        BookModel(528, "https://images.gr-assets.com/books/1320489812m/522776.jpg"),
        BookModel(480, "https://images.gr-assets.com/books/1291373267m/7846220.jpg"),
        BookModel(208, "http://covers.openlibrary.org/b/isbn/0876850867-L.jpg"),
        BookModel(null, "https://images.gr-assets.com/books/1336403527m/2608184.jpg"),
        BookModel(528, "http://covers.openlibrary.org/b/isbn/0062107062-L.jpg"),
        BookModel(416, "http://covers.openlibrary.org/b/isbn/143913376X-L.jpg"),
        BookModel(335, "https://images.gr-assets.com/books/1533117961m/17214.jpg"),
        BookModel(328, "http://covers.openlibrary.org/b/isbn/0671732471-L.jpg"),
        BookModel(384, "https://images.gr-assets.com/books/1274386753m/2524344.jpg"),
        BookModel(460, "https://images.gr-assets.com/books/1378626185m/18464870.jpg"),
        BookModel(195, "https://images.gr-assets.com/books/1499341314m/5460398.jpg"),
        BookModel(304, "http://covers.openlibrary.org/b/isbn/0671732501-L.jpg"),
        BookModel(291, "https://images.gr-assets.com/books/1325028693m/38500.jpg"),
        BookModel(126, "https://images.gr-assets.com/books/1457906509m/113205.jpg"),
        BookModel(260, "http://covers.openlibrary.org/b/isbn/1855384272-L.jpg"),
        BookModel(320, "https://images.gr-assets.com/books/1474154022m/3.jpg"),
        BookModel(116, "https://images.gr-assets.com/books/1535737556m/41580312.jpg"),
        BookModel(288, "https://images.gr-assets.com/books/1328705200m/110419.jpg"),
        BookModel(304, "https://images.gr-assets.com/books/1405259788m/12843.jpg"),
        BookModel(303, "http://covers.openlibrary.org/b/isbn/0060920432-L.jpg"),
        BookModel(480, "http://covers.openlibrary.org/b/isbn/523700878X-L.jpg"),
        BookModel(145, "http://covers.openlibrary.org/b/isbn/0525467564-L.jpg"),
        BookModel(288, "http://covers.openlibrary.org/b/isbn/0671227424-L.jpg"),
        BookModel(288, "http://covers.openlibrary.org/b/isbn/006092960X-L.jpg"),
        BookModel(540, "https://images.gr-assets.com/books/1410136019m/629.jpg"),
        BookModel(204, "https://images.gr-assets.com/books/1498631519m/95558.jpg"),
        BookModel(320, "https://images.gr-assets.com/books/1337690429m/13416454.jpg"),
        BookModel(2530, "https://images.gr-assets.com/books/1487328314m/13588846.jpg"),
        BookModel(288, "https://images.gr-assets.com/books/1353621838m/12844.jpg")
    )
}


fun fillBooks(models: List<BookModel>, books: MutableList<ARBookWithCover>) {

    makeGrid(models, books)
}

fun makeGrid(data: List<BookModel>, res: MutableList<ARBookWithCover>) {
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
            ARBookWithCover(
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




}

val offsets = listOf(
    Pair(-1.5f, -1.5f), Pair(-0.5f, -1.5f), Pair( 0.5f, -1.5f), Pair( 1.5f, -1.5f),
    Pair(-1.5f, -0.5f), Pair(-0.5f, -0.5f), Pair( 0.5f, -0.5f), Pair( 1.5f, -0.5f),
//                                         .
    Pair(-1.5f,  0.5f), Pair(-0.5f,  0.5f), Pair( 0.5f,  0.5f), Pair( 1.5f,  0.5f),
    Pair(-1.5f,  1.5f), Pair(-0.5f,  1.5f), Pair( 0.5f,  1.5f), Pair( 1.5f,  1.5f)

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