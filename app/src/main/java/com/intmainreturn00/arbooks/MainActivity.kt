package com.intmainreturn00.arbooks


import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.intmainreturn00.grapi.Order
import com.intmainreturn00.grapi.Review
import com.intmainreturn00.grapi.Sort
import com.intmainreturn00.grapi.grapi
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.jetbrains.anko.browse
import org.michaelevans.colorart.library.ColorArt
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class MainActivity : ScopedAppActivity() {

    private lateinit var fragment: ArFragment
    private lateinit var bookModel: ModelRenderable

    private val books = mutableListOf<ARBookWithCover>()

    val modelSize = Vector3(0.14903799f, 0.038000144f, 0.2450379f) // size of the obj model [in meters]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragment = sceneform_fragment as ArFragment

        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
            //onUpdate()
        }

        add.setOnClickListener {
            isHitPlane()?.let {
                launch {
                    placeBooks(it)
                }
            }
        }



        if (!grapi.isLoggedIn()) {
            launch {
                grapi.loginStart()
                browse(grapi.getAuthorizationUrl())
            }
        } else {
            loadResources()
        }

        launch {
            grapi.loginEnd(intent) { ok ->
                if (ok) {
                    loadResources()
                }
            }
        }

    }


    fun loadResources() {
        launch {
            bookModel = ModelRenderable.builder()
                .setSource(fragment.context, Uri.parse("book1.sfb"))
                .build().await()

            val userId = grapi.getUserId()
            val bookModels = mutableListOf<BookModel>()
            val reviews = grapi.getAllReviews(userId.id/*, sort = Sort.DATE_ADDED, order = Order.ASCENDING*/)

//            val sortedReviews =
//                reviews.sortedWith(
//                compareByDescending<Review> { it.rating ?: -1 }
//                    .thenByDescending { it.readCount ?: -1 }
//                    .thenByDescending { it.book.numPages }
//                    .thenByDescending { parseDate(it.readAt) }
//            )
            val sortedReviews = reviews.sortedWith(
                compareBy(
                    { it.readCount },
                    { it.rating }
                )
            )

            for (review in sortedReviews) {
                if (!review.book.imageUrl.contains("nophoto")) {
                    bookModels.add(BookModel(review.book.numPages, review.book.imageUrlSmall))
                } else {
                    println(review.book.title + " " + review.book.id)
                    if (review.book.isbn.isNotEmpty()) {
                        bookModels.add(
                            BookModel(
                                review.book.numPages,
                                "https://covers.openlibrary.org/b/isbn/${review.book.isbn}-M.jpg"
                            )
                        )
                    } else {
                        bookModels.add(BookModel(review.book.numPages, review.book.imageUrl))
                    }
                }
            }


//            repeat(20) {
//                println(sortedReviews[sortedReviews.size - it - 1].book.title + " " + sortedReviews[sortedReviews.size - it - 1].readCount)
//            }

            fillBooks(bookModels, books)
            println("models loaded")
        }
        add.show()
    }

    private fun isHitPlane(): Anchor? {
        val frame = fragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    return hit.createAnchor()
                }
            }
        }
        return null
    }


    private suspend fun placeBooks(anchor: Anchor) {
        val anchorNode = AnchorNode(anchor)
        fragment.arSceneView.scene.addChild(anchorNode)


        for (book in books) {
            val node = makeBookNode(bookModel.makeCopy(), book.size, book.position, book.rotation)
            val coverRenderable = loadCoverRenderable(this)
            val cover = downloadBitmap2(this, book.coverUrl)!!
            addCover(cover, node, (node.renderable as ModelRenderable).collisionShape as Box, coverRenderable)
            paintBook(cover, node)
            node.setParent(anchorNode)
            println("add another book")
        }


        fragment.arSceneView.planeRenderer.isVisible = false

        add.hide()
    }


    fun makeBookNode(model: ModelRenderable, size: Vector3, position: Vector3, rotation: Quaternion): Node {
        val book = Node()
        book.renderable = model

        book.localScale = Vector3(
            size.x / modelSize.x,
            size.y / modelSize.y,
            size.z / modelSize.z
        )
        book.localRotation = rotation
        book.localPosition = position
        return book
    }


    fun paintBook(btm: Bitmap, node: Node) {
        val palette = ColorArt(btm)

        node.renderable = node.renderable!!//.makeCopy()
        val mat1 = node.renderable!!.getMaterial(1)//.makeCopy()
        mat1.setFloat3("baseColorTint", com.google.ar.sceneform.rendering.Color(palette.backgroundColor))
        node.renderable!!.setMaterial(1, mat1)

        val mat2 = node.renderable!!.getMaterial(2)//.makeCopy()
        mat2.setFloat3("baseColorTint", com.google.ar.sceneform.rendering.Color(palette.backgroundColor))
        node.renderable!!.setMaterial(2, mat1)
    }


    fun addCover(btm: Bitmap, parent: Node, parentBox: Box, model: ViewRenderable) {
        val coverNode = Node()
        coverNode.renderable = model

        val img = (coverNode.renderable as ViewRenderable).view as ImageView
        img.setImageBitmap(btm)

        coverNode.localPosition = Vector3(0f, modelSize.y, 0.005f - modelSize.z / 2)

        val realXm = btm.width / dpToPix(250f)
        val realYm = btm.height / dpToPix(250f)

        coverNode.localScale = Vector3(0.87f * parentBox.size.x / realXm, 0.87f * parentBox.size.z / realYm, 1f)

        coverNode.localRotation = Quaternion.axisAngle(Vector3(1.0f, 0.0f, 0.0f), 90f)

        coverNode.setParent(parent)
    }


    private fun getScreenCenter(): Point {
        val vw = findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }

    private fun dpToPix(dp: Float): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
}
