package com.intmainreturn00.arbooks.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.intmainreturn00.arbooks.*
import com.intmainreturn00.arbooks.platform.ScopedFragment
import com.intmainreturn00.arbooks.ui.PodkovaFont
import com.intmainreturn00.arbooks.ui.setCustomFont
import com.intmainreturn00.arbooks.viewmodels.BooksViewModel
import com.intmainreturn00.grapi.grapi
import kotlinx.android.synthetic.main.fragment_loading.*
import kotlinx.coroutines.launch

class LoadingFragment : ScopedFragment() {

    private val model by activityViewModels<BooksViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loading.setCustomFont(PodkovaFont.REGULAR)

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

        model.booksLoadingDone.observe(this, Observer { done ->
                if (done) {
                    findNavController().navigate(R.id.action_loading_to_shelves)
                }
            })

    }


    private fun prefetch() =
        ViewModelProviders.of(activity!!).get(BooksViewModel::class.java).loadBooks()

}
