package com.intmainreturn00.arbooks.fragments


import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.intmainreturn00.arbooks.*

import kotlinx.android.synthetic.main.fragment_ar.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch


class ARFragment : ScopedFragment() {

    private lateinit var fragment: ArFragment
    private lateinit var bookModel: ModelRenderable
    private val modelSize = Vector3(0.14903799f, 0.038000144f, 0.2450379f)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment = childFragmentManager.findFragmentById(R.id.ar_sceneform_fragment) as ArFragment
        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
        }

        launch { loadResources() }

        activity?.run {
            val model = ViewModelProviders.of(this).get(BooksViewModel::class.java)
            PodkovaFont.EXTRA_BOLD.apply(ar_books_num, ar_books, ar_pages_num, ar_pages, ar_age_num, ar_age, ar_shelf_title)
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
                ar_shelf_title.text = model.shelves[model.selectedShelves.first()].name
            } else {
                ar_shelf_title.visibility = INVISIBLE
            }

            hideSystemUI()

            //header.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            header.viewTreeObserver.addOnWindowFocusChangeListener { hasFocus ->
                if (hasFocus)
                    hideSystemUI()
            }

        }

    }


    private suspend fun loadResources() {
        bookModel = ModelRenderable.builder()
            .setSource(fragment.context, Uri.parse("book1.sfb"))
            .build().await()
        //ar_grid.visibility = VISIBLE
        //ar_controls.visibility = VISIBLE
    }


    private fun getScreenCenter(): Point? {
        activity?.findViewById<View>(android.R.id.content)?.let {
            return@let Point(it.width / 2, it.height / 2)
        }
        return null
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
