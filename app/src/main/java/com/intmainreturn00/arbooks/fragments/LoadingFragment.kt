package com.intmainreturn00.arbooks.fragments


import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.intmainreturn00.arbooks.BooksViewModel
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
                prefetch()
            } else if (activity?.intent?.data != null) {
                grapi.loginEnd(activity?.intent!!) { ok ->
                    if (ok) {
                        prefetch()
                    }
                }
            }
        }

        ViewModelProviders.of(this@LoadingFragment).get(BooksViewModel::class.java).currentShelf.observe(
            this,
            Observer { currentShelf ->
                loading.text = String.format(resources.getString(R.string.loading_from), currentShelf)
            })

        ViewModelProviders.of(this@LoadingFragment).get(BooksViewModel::class.java).loadingDone.observe(
            this,
            Observer { done ->
                if (done) {

                }
            })

    }


    private fun prefetch() =
        ViewModelProviders.of(this@LoadingFragment).get(BooksViewModel::class.java).loadProfileData()

}
