package com.intmainreturn00.arbooks.fragments


import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intmainreturn00.arbooks.BookModel

import com.intmainreturn00.arbooks.R
import com.intmainreturn00.arbooks.ScopedFragment
import com.intmainreturn00.grapi.grapi
import kotlinx.android.synthetic.main.fragment_loading.*
import kotlinx.coroutines.launch


class LoadingFragment : ScopedFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loading.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-Regular.ttf")

        launch {
            if (grapi.isLoggedIn()) {
                loadData()
            } else if (activity?.intent != null) {
                grapi.loginEnd(activity?.intent!!) { ok ->
                    if (ok) {
                        loadData()
                    }
                }
            }
        }

    }


    fun loadData() {
        launch {
            val userId = grapi.getUserId()
            val bookModels = mutableListOf<BookModel>()
            //val reviews = grapi.getAllReviews(userId.id)
            val shelves = grapi.getUserShelves(1, userId.id)

            println("@")
        }
    }

}
