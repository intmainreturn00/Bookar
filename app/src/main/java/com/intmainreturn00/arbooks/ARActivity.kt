package com.intmainreturn00.arbooks


import android.graphics.*
import android.net.Uri
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_ar.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch


class ARActivity : ScopedAppActivity() {

    private lateinit var fragment: ArFragment
    private lateinit var bookModel: ModelRenderable
    private val modelSize = Vector3(0.14903799f, 0.038000144f, 0.2450379f) // size of the obj model [in meters]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        System.setProperty("kotlinx.coroutines.debug", "on")

        text.typeface = Typeface.createFromAsset(assets, "fonts/Podkova-Medium.ttf")

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

        share.setOnClickListener {
            share.hide()
            takePhoto(this@ARActivity, fragment)
            share.show()
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

        for (book in App.books) {
            val bookNode = makeBookNode(bookModel.makeCopy(), book)
            addCover(book, bookNode)
            bookNode.setParent(anchorNode)
        }

        share.show()
        text.text = "#bookar"
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

    private suspend fun addCover(book: ARBook, bookNode: Node) {
        val btm = downloadImage(this@ARActivity, book.coverUrl)
        if (btm != null) {
            val coverNode = Node()
            coverNode.renderable = loadCoverRenderable(this, book.coverType)
            val parentBox = (bookNode.renderable as ModelRenderable).collisionShape as Box

            val img = (coverNode.renderable as ViewRenderable).view as ImageView

            coverNode.localPosition = Vector3(0f, modelSize.y, 0.005f - modelSize.z / 2)

            val realXm = book.coverWidth / dpToPix(this,250f)
            val realYm = book.coverHeight / dpToPix(this, 250f)

            img.setImageBitmap(btm)

            coverNode.localScale = Vector3(0.87f * parentBox.size.x / realXm, 0.87f * parentBox.size.z / realYm, 1f)
            coverNode.localRotation = Quaternion.axisAngle(Vector3(1.0f, 0.0f, 0.0f), 90f)

            coverNode.setParent(bookNode)

            paintBook(bookNode, book.coverColor)

        } else {
            paintBook(bookNode)
        }
    }


    private fun paintBook(bookNode: Node, color: Int = makeRandomColor()) {
        val backgroundColor = com.google.ar.sceneform.rendering.Color(color)

        val mat1 = bookNode.renderable!!.getMaterial(1)
        mat1.setFloat3("baseColorTint", backgroundColor)
        bookNode.renderable!!.setMaterial(1, mat1)

        val mat2 = bookNode.renderable!!.getMaterial(2)
        mat2.setFloat3("baseColorTint", backgroundColor)
        bookNode.renderable!!.setMaterial(2, mat2)

    }


    private fun getScreenCenter(): Point {
        val vw = findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }

}
