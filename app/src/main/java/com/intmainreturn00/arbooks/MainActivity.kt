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
import com.intmainreturn00.grapi.grapi
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.browse
import kotlin.random.Random


class MainActivity : ScopedAppActivity() {

    private lateinit var fragment: ArFragment
    private lateinit var bookModel: ModelRenderable
    private lateinit var books: MutableList<ARBook>
    private val modelSize = Vector3(0.14903799f, 0.038000144f, 0.2450379f) // size of the obj model [in meters]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragment = sceneform_fragment as ArFragment

        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
        }

        add.setOnClickListener {
            isHitPlane()?.let {
                launch {
                    placeBooks(it)
                }
            }
        }

        capture.setOnClickListener {
            capture.hide()
            takePhoto(this@MainActivity, fragment)
            capture.show()
        }

        if (!grapi.isLoggedIn()) {
            launch {
                grapi.loginStart()
                browse(grapi.getAuthorizationUrl())
            }
        } else {
            launch { loadResources() }
        }

        launch {
            grapi.loginEnd(intent) { ok ->
                if (ok) {
                    launch { loadResources() }
                }
            }
        }

        //launch { loadResources() }

        //println("@ ${generateFilename()}")

    }


    private suspend fun loadResources() {
        bookModel = ModelRenderable.builder()
            .setSource(fragment.context, Uri.parse("book1.sfb"))
            .build().await()

        val userId = grapi.getUserId()
        val bookModels = mutableListOf<BookModel>()
        val reviews = grapi.getAllReviews(userId.id/*, sort = Sort.DATE_ADDED, order = Order.ASCENDING*/)

        withContext(Dispatchers.Default) {
            val sortedReviews = reviews.sortedWith(
                compareBy(
                    { it.readCount },
                    { it.rating }
                )
            )//.takeLast(20)

            for (review in sortedReviews) {
                when {
                    !review.book.imageUrl.contains("nophoto") ->
                        bookModels.add(BookModel(review.book.numPages, review.book.imageUrl))

                    review.book.isbn.isNotEmpty() ->
                        bookModels.add(BookModel(review.book.numPages, makeOpenlibLink(review.book.isbn)))

                    else -> bookModels.add(BookModel(review.book.numPages, ""))
                }
            }

            books = fillBooks(this@MainActivity, bookModels)

        }

        println("@ models loaded")
        text.text = "loaded!"
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
        fragment.arSceneView.planeRenderer.isVisible = false

        val layer = MutableList<MutableList<Node>>(2) { mutableListOf() }
        var counter = 0
        for (book in books) {
            val bookNode = makeBookNode(bookModel.makeCopy(), book)
            addCover(book, bookNode)
            layer[counter / 16].add(bookNode)

            bookNode.setParent(anchorNode)
            counter++

            if (counter == 16 * 2 - 1) {
                // clean up overlapped 0 layer
                for (node in layer[0]) {
                    if (node.children.size > 0) {
                        node.removeChild(node.children[0])
                    }
                }
                layer.removeAt(0)
                layer.add(mutableListOf())
                counter = 0
            }
        }

        add.hide()
        capture.show()

    }


    private fun makeBookNode(model: ModelRenderable, book: ARBook): Node {
        val bookNode = Node()
        bookNode.renderable = model

        bookNode.localScale = Vector3(
            book.size.x / modelSize.x,
            book.size.y / modelSize.y,
            book.size.z / modelSize.z
        )
        bookNode.localRotation = book.rotation
        bookNode.localPosition = book.position
        return bookNode
    }


    private suspend fun addCover(book: ARBook, parent: Node) : Node? {
        val btm = downloadCover(this@MainActivity, book)
        if (btm != null) {
            val coverNode = Node()
            coverNode.renderable = loadCoverRenderable(this)
            val parentBox = (parent.renderable as ModelRenderable).collisionShape as Box
            val img = (coverNode.renderable as ViewRenderable).view as ImageView

            coverNode.localPosition = Vector3(0f, modelSize.y, 0.005f - modelSize.z / 2)

            val realXm = book.width / dpToPix(250f)
            val realYm = book.height / dpToPix(250f)

            img.setImageBitmap(btm)

            coverNode.localScale = Vector3(0.87f * parentBox.size.x / realXm, 0.87f * parentBox.size.z / realYm, 1f)
            coverNode.localRotation = Quaternion.axisAngle(Vector3(1.0f, 0.0f, 0.0f), 90f)

            coverNode.setParent(parent)

            paintBook(btm, parent)

            return coverNode
        } else {
            paintBook(parent)
            return null
        }
    }

    private suspend fun paintBook(btm: Bitmap, node: Node) {
//        val palette: ColorArt = withContext(Dispatchers.Default) {
//            ColorArt(btm)
//        }

        val backgroundColor: Int = btm.getPixel(Random.nextInt(btm.width), Random.nextInt(btm.height))
        //val backgroundColor: Int = palette.backgroundColor

        node.renderable = node.renderable!!//.makeCopy()
        val mat1 = node.renderable!!.getMaterial(1)//.makeCopy()
        mat1.setFloat3("baseColorTint", com.google.ar.sceneform.rendering.Color(backgroundColor))
        node.renderable!!.setMaterial(1, mat1)

        val mat2 = node.renderable!!.getMaterial(2)//.makeCopy()
        mat2.setFloat3("baseColorTint", com.google.ar.sceneform.rendering.Color(backgroundColor))
        node.renderable!!.setMaterial(2, mat1)
    }


    private fun paintBook(node: Node) {
        val backgroundColor: Int = makeRandomColor()

        node.renderable = node.renderable!!//.makeCopy()
        val mat1 = node.renderable!!.getMaterial(1)//.makeCopy()
        mat1.setFloat3("baseColorTint", com.google.ar.sceneform.rendering.Color(backgroundColor))
        node.renderable!!.setMaterial(1, mat1)

        val mat2 = node.renderable!!.getMaterial(2)//.makeCopy()
        mat2.setFloat3("baseColorTint", com.google.ar.sceneform.rendering.Color(backgroundColor))
        node.renderable!!.setMaterial(2, mat1)
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
