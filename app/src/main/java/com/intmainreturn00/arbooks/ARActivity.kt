package com.intmainreturn00.arbooks


import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Typeface
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch


class ARActivity : ScopedAppActivity() {

    private lateinit var fragment: ArFragment
    private lateinit var bookModel: ModelRenderable
    private val modelSize = Vector3(0.14903799f, 0.038000144f, 0.2450379f) // size of the obj model [in meters]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.setProperty("kotlinx.coroutines.debug", "on")

        fragment = sceneform_fragment as ArFragment

        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
        }

        stats.typeface = Typeface.createFromAsset(assets, "fonts/FredokaOne-Regular.ttf")
        hashtag.typeface = Typeface.createFromAsset(assets, "fonts/FredokaOne-Regular.ttf")

        val pagesFormatted =
            if (App.totalPages > 1000) {
                (App.totalPages / 1000).toString() + "k " + resources.getString(R.string.pages)
            } else {
                resources.getQuantityString(R.plurals.pages, App.totalPages, App.totalPages)
            }

        stats.text = "${resources.getQuantityString(R.plurals.books, App.books.size, App.books.size)}" +
                "\n$pagesFormatted"

        add.setOnClickListener {
            isHitPlane()?.let {
                launch {
                    placeBooks(it)
                }
            }
        }

        capture.setOnClickListener {
            capture.hide()
            takePhoto(this@ARActivity, fragment, stats_card, hashtag)
            capture.show()
        }

        launch { loadResources() }

    }


    private suspend fun loadResources() {
        bookModel = ModelRenderable.builder()
            .setSource(fragment.context, Uri.parse("book1.sfb"))
            .build().await()

        println("@ models loaded")
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
        add.hide()

        val layer = MutableList<MutableList<Node>>(2) { mutableListOf() }
        var counter = 0
        for (book in App.books) {
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


    private suspend fun addCover(book: ARBook, parent: Node): Node? {
        val btm = downloadCover(this@ARActivity, book)
        if (btm != null) {
            val coverNode = Node()
            coverNode.renderable = loadCoverRenderable(this)
            val parentBox = (parent.renderable as ModelRenderable).collisionShape as Box
            val img = (coverNode.renderable as ViewRenderable).view as ImageView

            coverNode.localPosition = Vector3(0f, modelSize.y, 0.005f - modelSize.z / 2)

            val realXm = book.width / dpToPix(this,250f)
            val realYm = book.height / dpToPix(this, 250f)

            img.setImageBitmap(btm)

            coverNode.localScale = Vector3(0.87f * parentBox.size.x / realXm, 0.87f * parentBox.size.z / realYm, 1f)
            coverNode.localRotation = Quaternion.axisAngle(Vector3(1.0f, 0.0f, 0.0f), 90f)

            coverNode.setParent(parent)

            paintBook(book, btm, parent)

            return coverNode
        } else {
            paintBook(parent)
            return null
        }
    }

    private fun paintBook(book: ARBook, btm: Bitmap, node: Node) {
        val backgroundColor: Int = book.coverColor

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

}
