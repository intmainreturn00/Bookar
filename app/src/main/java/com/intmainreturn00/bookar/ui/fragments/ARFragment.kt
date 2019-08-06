package com.intmainreturn00.bookar.ui.fragments


import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Point
import android.media.CamcorderProfile
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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
import com.intmainreturn00.bookar.*
import com.intmainreturn00.bookar.data.network.downloadImage
import com.intmainreturn00.bookar.domain.*
import com.intmainreturn00.bookar.platform.ScopedFragment
import com.intmainreturn00.bookar.ui.*
import com.intmainreturn00.bookar.viewmodels.BooksViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_ar.*
import kotlinx.android.synthetic.main.fragment_ar.header
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.future.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Interface Adapters
fun MyVector3.toVector3() = Vector3(x, y, z)
fun MyQuaternion.toQuaternion() = Quaternion.axisAngle(Vector3(x, y, z), w)

// styled toast
fun Context.makeToast(s: String) {
    val t = Toasty.normal(this, s)
    t.setGravity(Gravity.BOTTOM, 0, dpToPix(this, 80f).toInt())
    t.show()
}

class ARFragment : ScopedFragment() {
    private val model by activityViewModels<BooksViewModel>()
    private lateinit var fragment: ArFragment
    private lateinit var bookModel: ModelRenderable
    private val modelSize = Vector3(0.14903799f, 0.038000144f, 0.2450379f)
    private var anchorNode: AnchorNode? = null
    private val nodes = mutableListOf<Node>()
    private var rootAnchor: Anchor? = null
    private var currentAllocationType: AllocationType = AllocationType.GridType
    private var videoRecorder = VideoRecorder()
    private var firstPlacement = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment = childFragmentManager.findFragmentById(R.id.ar_sceneform_fragment) as ArFragment
        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
        }

        PodkovaFont.EXTRA_BOLD.apply(
            ar_books_num, ar_books,
            ar_pages_num, ar_pages,
            ar_age_num, ar_age,
            ar_shelf_title
        )

        model.user.observe(this, Observer { updateStatistics(it) })

        val title = model.isExactlyOneShelfSelected()
        if (title != null) {
            ar_shelf_title.text = title
            ar_shelf_title.visibility = VISIBLE
        } else {
            ar_shelf_title.visibility = GONE
        }

        hideSystemUI()
        header.viewTreeObserver.addOnWindowFocusChangeListener { hasFocus ->
            if (hasFocus)
                hideSystemUI()
        }

        videoRecorder.setSceneView(fragment.arSceneView)
        videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_1080P, ORIENTATION_PORTRAIT)

        // first time - shuffle button == grid
        ar_shuffle.setOnClickListener {
            isHitPlane()?.let {
                ar_placement.visibility = GONE
                ar_shuffle.visibility = GONE
                cleanupAll()
                rootAnchor = it
                if (firstPlacement) {
                    model.allocateBooksInAR(AllocationType.GridType)
                    ar_shuffle.setImageResource(R.drawable.refresh)
                } else {
                    model.shuffle(currentAllocationType)
                }
                firstPlacement = false
            }
        }

        ar_placement.setOnClickListener {
            isHitPlane()?.let {
                launch {
                    ar_placement.visibility = GONE
                    ar_shuffle.visibility = GONE
                    if (firstPlacement) {
                        ar_shuffle.setImageResource(R.drawable.refresh)
                        firstPlacement = false
                    }
                    cleanupAll()
                    rootAnchor = it
                    if (currentAllocationType == AllocationType.GridType) {
                        currentAllocationType = AllocationType.TowersType
                        ar_placement.setImageResource(R.drawable.grid)
                    } else {
                        currentAllocationType = AllocationType.GridType
                        ar_placement.setImageResource(R.drawable.tower)

                    }
                    model.allocateBooksInAR(currentAllocationType)
                }
            }
        }

        model.arbooks.observe(this, Observer { arbooks ->
            rootAnchor?.let { anchor ->
                launch { placeBooks(anchor, arbooks) }
            }
        })

        capture.setOnClickListener {
            if (videoRecorder.isRecording) {
                it.background = activity!!.getDrawable(R.drawable.camera)
                toggleRecording()
                activity?.makeToast("Video saved to AR_Books/")
            } else {
                takePhoto(activity!!, fragment, header)
                activity?.makeToast("Image saved to AR_Books/")
            }
        }

        capture.setOnLongClickListener {
            it.background = activity!!.getDrawable(R.drawable.hold)
            toggleRecording()
            true
        }

    }


    override fun onPause() {
        super.onPause()
        if (videoRecorder.isRecording) {
            toggleRecording()
        }
        job.cancel()
    }


    private fun toggleRecording() {
        val recording = videoRecorder.onToggleRecord()
        if (!recording) {
            val path = videoRecorder.videoPath.absolutePath
            activity?.let {
                it.makeToast("Video saved to AR_Books/")
                val values = ContentValues()
                values.put(MediaStore.Video.Media.TITLE, "BOOKAR Video")
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                values.put(MediaStore.Video.Media.DATA, path)
                it.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }


    private suspend fun loadResources() {
        if (!this::bookModel.isInitialized) {
            bookModel = ModelRenderable.builder()
                .setSource(fragment.context, Uri.parse("book1.sfb"))
                .build().await()
        }
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


    private suspend fun placeBooks(anchor: Anchor, arbooks: List<ARBook>) {
        anchorNode = AnchorNode(anchor)
        fragment.arSceneView.scene.addChild(anchorNode)
        fragment.arSceneView.planeRenderer.isVisible = false
        ar_placement.visibility = GONE
        ar_shuffle.visibility = GONE

        loadResources()

        val layer = MutableList<MutableList<Node>>(2) { mutableListOf() }
        var counter = 0
        for (book in arbooks) {
            if (!isActive) {
                throw CancellationException()
            }
            val bookNode = makeBookNode(bookModel.makeCopy(), book)
            nodes.add(bookNode)
            addCover(book, bookNode)
            if (!book.isTopBook) {
                layer[counter / 16].add(bookNode)
                counter++
            }
            bookNode.setParent(anchorNode)

            if (counter == 16 * 2 - 1) {
                // clean up overlapped 0 layer to gain performance
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

        ar_placement.visibility = VISIBLE
        ar_shuffle.visibility = VISIBLE
    }


    private fun makeBookNode(model: ModelRenderable, book: ARBook): Node {
        val bookNode = Node()
        bookNode.renderable = model

        bookNode.localScale = Vector3(
            book.size.x / modelSize.x,
            book.size.y / modelSize.y,
            book.size.z / modelSize.z
        )
        bookNode.localRotation = book.rotation.toQuaternion()
        bookNode.localPosition = book.position.toVector3()
        return bookNode
    }


    private suspend fun addCover(book: ARBook, bookNode: Node) {
        val coverNode = Node()
        coverNode.renderable = loadRenderableForCover(book.bookModel.cover)
        val parentBox = (bookNode.renderable as ModelRenderable).collisionShape as Box
        val realXm = book.bookModel.cover.width / dpToPix(context!!, 250f)
        val realYm = book.bookModel.cover.height / dpToPix(context!!, 250f)

        coverNode.localPosition = Vector3(0f, modelSize.y, 0.005f - modelSize.z / 2)
        coverNode.localScale = Vector3(0.87f * parentBox.size.x / realXm, 0.87f * parentBox.size.z / realYm, 1f)
        coverNode.localRotation = Quaternion.axisAngle(Vector3(1.0f, 0.0f, 0.0f), 90f)

        when (book.bookModel.cover) {
            is ImageCover -> {
                val btm = downloadImage(context!!, book.bookModel.cover.url)
                val img = (coverNode.renderable as ViewRenderable).view as ImageView
                img.setImageBitmap(btm)
            }
            is TemplateCover -> {
                val root = (coverNode.renderable as ViewRenderable).view as FrameLayout

                root.findViewById<TextView>(R.id.author)?.text = book.bookModel.author
                root.findViewById<TextView>(R.id.title)?.text = book.bookModel.title
                root.findViewById<TextView>(R.id.title)?.setTextColor(book.bookModel.cover.textColor)
                root.findViewById<TextView>(R.id.author)?.setTextColor(book.bookModel.cover.textColor)
                root.findViewById<View>(R.id.background).setBackgroundColor(book.bookModel.cover.spineColor)
            }
        }

        coverNode.setParent(bookNode)
        paintBook(bookNode, book.bookModel.cover.spineColor)
    }

    private suspend fun loadRenderableForCover(cover: Cover) = when (cover) {
        is ImageCover -> ViewRenderable.builder().setView(context, R.layout.cover).build().await()
        is TemplateCover -> ViewRenderable.builder().setView(context, R.layout.book_template).build().await()
    }


    private fun paintBook(bookNode: Node, color: Int) {
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

    private fun updateStatistics(user: User) {
        if (user.numBooks != null && user.numPages != null) {
            ar_books_num.text = user.numBooks.toString()
            ar_books.text = resources.getQuantityString(R.plurals.books, user.numBooks)
            ar_books_num.visibility = VISIBLE
            ar_books.visibility = VISIBLE

            ar_pages_num.text = user.numPages.toString()
            ar_pages.text = resources.getQuantityString(R.plurals.pages, user.numPages)
            ar_pages_num.visibility = VISIBLE
            ar_pages.visibility = VISIBLE

            ar_age_num.visibility = VISIBLE
            ar_age.visibility = VISIBLE
            val (num, qualifier) = formatProfileAge(user.joined)
            ar_age_num.text = num
            ar_age.text = qualifier
        } else {
            ar_books_num.visibility = View.INVISIBLE
            ar_books.visibility = View.INVISIBLE
            ar_pages_num.visibility = View.INVISIBLE
            ar_pages.visibility = View.INVISIBLE
            ar_age_num.visibility = View.INVISIBLE
            ar_age.visibility = View.INVISIBLE
        }
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
