package com.intmainreturn00.arbooks.fragments


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

import com.intmainreturn00.arbooks.R
import com.intmainreturn00.arbooks.ScopedFragment
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

    }


    private suspend fun loadResources() {
        bookModel = ModelRenderable.builder()
            .setSource(fragment.context, Uri.parse("book1.sfb"))
            .build().await()

    }


}
