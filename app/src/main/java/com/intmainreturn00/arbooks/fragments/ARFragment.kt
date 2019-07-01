package com.intmainreturn00.arbooks.fragments


import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import com.intmainreturn00.arbooks.*
import kotlinx.android.synthetic.main.fragment_ar.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch


class ARFragment : ScopedFragment() {

    private lateinit var fragment: ArFragment
    private lateinit var bookModel: ModelRenderable
    private val modelSize = Vector3(0.14903799f, 0.038000144f, 0.2450379f)
    private var anchorNode: AnchorNode? = null
    private val nodes = mutableListOf<Node>()
    private var rootAnchor: Anchor? = null
    private var currentPlacement: PLACEMENT = PLACEMENT.GRID

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment = childFragmentManager.findFragmentById(R.id.ar_sceneform_fragment) as ArFragment
        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
        }

        activity?.run {
            val model = ViewModelProviders.of(this).get(BooksViewModel::class.java)
            PodkovaFont.EXTRA_BOLD.apply(
                ar_books_num,
                ar_books,
                ar_pages_num,
                ar_pages,
                ar_age_num,
                ar_age,
                ar_shelf_title
            )
            PodkovaFont.REGULAR.apply(ar_share)

            ar_books_num.text = model.numBooks.toString()
            ar_books.text = resources.getQuantityString(R.plurals.books, model.numBooks)
            ar_pages_num.text = model.numPages.toString()
            ar_pages.text = resources.getQuantityString(R.plurals.pages, model.numPages)
            val (num, qualifier) = formatProfileAge(model.user.joined)
            ar_age_num.text = num
            ar_age.text = qualifier

            if (model.selectedShelves.size == 1) {
                ar_shelf_title.visibility = VISIBLE
                ar_shelf_title.text = model.selectedShelves.first()
            } else {
                ar_shelf_title.visibility = GONE
            }

            ar_first_placement.visibility = VISIBLE
            ar_controls.visibility = GONE

            hideSystemUI()
            header.viewTreeObserver.addOnWindowFocusChangeListener { hasFocus ->
                if (hasFocus)
                    hideSystemUI()
            }
        }

    }

    override fun onResume() {
        super.onResume()

        activity?.run {
            val model = ViewModelProviders.of(this).get(BooksViewModel::class.java)

            model.arbooks.observe(this, Observer {
                rootAnchor?.let { launch { placeBooks(it) } }
            })

            ar_first_placement.setOnClickListener {
                isHitPlane()?.let {
                    model.moveBooksToAR(PLACEMENT.GRID)
                    rootAnchor = it
                    ar_first_placement.visibility = GONE
                }
            }

            ar_placement.setOnClickListener {
                isHitPlane()?.let {
                    launch {
                        ar_controls.visibility = GONE
                        cleanupAll()
                        rootAnchor = it
                        if (currentPlacement == PLACEMENT.GRID) {
                            currentPlacement = PLACEMENT.TOWER
                            ar_placement.setImageResource(R.drawable.grid)
                        } else {
                            currentPlacement = PLACEMENT.GRID
                            ar_placement.setImageResource(R.drawable.tower)

                        }
                        model.moveBooksToAR(currentPlacement)
                    }
                }
            }

            ar_share.setOnClickListener {
                ar_controls.visibility = GONE
                takePhoto(this, fragment, header) {
                    ar_controls.visibility = VISIBLE
                    if (it.isNotEmpty()) {
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "image/jpeg"
                        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(it))
                        activity?.startActivity(Intent.createChooser(share, "Select"))
                    }
                }
            }

            ar_shuffle.setOnClickListener {
                ar_controls.visibility = GONE
                cleanupAll()
                model.shuffle(currentPlacement)
            }
        }
    }


    private suspend fun loadResources() {
        bookModel = ModelRenderable.builder()
            .setSource(fragment.context, Uri.parse("book1.sfb"))
            .build().await()
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
        anchorNode = AnchorNode(anchor)
        fragment.arSceneView.scene.addChild(anchorNode)
        fragment.arSceneView.planeRenderer.isVisible = false
        ar_first_placement.visibility = GONE
        ar_controls.visibility = GONE

        loadResources()
        for (book in ViewModelProviders.of(activity!!).get(BooksViewModel::class.java).arbooks.value!!) {
            val bookNode = makeBookNode(bookModel.makeCopy(), book)
            nodes.add(bookNode)
            addCover(book, bookNode)
            bookNode.setParent(anchorNode)
        }

        ar_controls.visibility = VISIBLE
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
        val btm = downloadImage(context!!, book.coverUrl)
        if (btm != null) {
            val coverNode = Node()
            coverNode.renderable = loadCoverRenderable(context!!, book.coverType)
            val parentBox = (bookNode.renderable as ModelRenderable).collisionShape as Box

            val img = (coverNode.renderable as ViewRenderable).view as ImageView

            coverNode.localPosition = Vector3(0f, modelSize.y, 0.005f - modelSize.z / 2)

            val realXm = book.coverWidth / dpToPix(context!!, 250f)
            val realYm = book.coverHeight / dpToPix(context!!, 250f)

            img.setImageBitmap(btm)

            coverNode.localScale = Vector3(0.87f * parentBox.size.x / realXm, 0.87f * parentBox.size.z / realYm, 1f)
            coverNode.localRotation = Quaternion.axisAngle(Vector3(1.0f, 0.0f, 0.0f), 90f)

            coverNode.setParent(bookNode)

            paintBook(bookNode, book.coverColor)

        } else {

            val coverNode = Node()
            coverNode.renderable = loadCoverRenderable(context!!, book.coverType)
            val parentBox = (bookNode.renderable as ModelRenderable).collisionShape as Box

            val root = (coverNode.renderable as ViewRenderable).view as FrameLayout
            coverNode.localPosition = Vector3(0f, modelSize.y, 0.005f - modelSize.z / 2)

            val realXm = book.coverWidth / dpToPix(context!!, 250f)
            val realYm = book.coverHeight / dpToPix(context!!, 250f)

            root.findViewById<TextView>(R.id.title)?.text = book.title
            root.findViewById<TextView>(R.id.title)?.setTextColor(book.textColor)
            root.findViewById<TextView>(R.id.author1)?.setTextColor(book.textColor)
            root.findViewById<TextView>(R.id.author2)?.setTextColor(book.textColor)

            root.findViewById<View>(R.id.background).setBackgroundColor(book.coverColor)

            when (book.authors.size) {
                0 -> {
                    root.findViewById<TextView>(R.id.author1)?.text = ""
                    root.findViewById<TextView>(R.id.author2)?.text = ""
                    root.findViewById<TextView>(R.id.author1)?.visibility = View.GONE
                    root.findViewById<TextView>(R.id.author2)?.visibility = View.GONE
                }
                1 -> {
                    root.findViewById<TextView>(R.id.author1)?.text = book.authors[0]
                    root.findViewById<TextView>(R.id.author2)?.text = ""
                    root.findViewById<TextView>(R.id.author1)?.visibility = VISIBLE
                    root.findViewById<TextView>(R.id.author2)?.visibility = View.GONE
                }
                else -> {
                    root.findViewById<TextView>(R.id.author1)?.text = book.authors[0]
                    root.findViewById<TextView>(R.id.author2)?.text = book.authors[1]
                    root.findViewById<TextView>(R.id.author1)?.visibility = VISIBLE
                    root.findViewById<TextView>(R.id.author2)?.visibility = VISIBLE
                }
            }

            coverNode.localScale = Vector3(0.87f * parentBox.size.x / realXm, 0.87f * parentBox.size.z / realYm, 1f)
            coverNode.localRotation = Quaternion.axisAngle(Vector3(1.0f, 0.0f, 0.0f), 90f)

            coverNode.setParent(bookNode)

            paintBook(bookNode, book.coverColor)
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


    private fun cleanupAll() {
        if (anchorNode != null) {
            for (n in nodes) {
                anchorNode!!.removeChild(n)
            }
        }
    }


    private fun getScreenCenter(): Point {
        val screen = activity!!.findViewById<View>(android.R.id.content)
        return Point(screen.width / 2, screen.height / 2)
    }


    private fun hideSystemUI() {
        activity?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


    private fun showSystemUI() {
        activity?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }


}
