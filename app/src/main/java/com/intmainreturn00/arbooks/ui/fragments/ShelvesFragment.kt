package com.intmainreturn00.arbooks.ui.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.intmainreturn00.arbooks.*
import com.intmainreturn00.arbooks.platform.GlideApp
import com.intmainreturn00.arbooks.ui.PodkovaFont
import com.intmainreturn00.arbooks.ui.ShelvesAdapter
import com.intmainreturn00.arbooks.viewmodels.BooksViewModel

import kotlinx.android.synthetic.main.fragment_shelves.*

class ShelvesFragment : Fragment() {
    private lateinit var adapter: ShelvesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shelves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.run {
            val model = ViewModelProviders.of(this).get(BooksViewModel::class.java)
            GlideApp.with(this).load(model.user.avatar).apply(RequestOptions().circleCrop()).into(avatar)

            books_num.text = model.numBooks.toString()
            books.text = resources.getQuantityString(R.plurals.books, model.numBooks)
            pages_num.text = model.numPages.toString()
            pages.text = resources.getQuantityString(R.plurals.pages, model.numPages)
            val (num, qualifier) = formatProfileAge(model.user.joined)
            age_num.text = num
            age.text = qualifier

            PodkovaFont.EXTRA_BOLD.apply(books_num, books, pages_num, pages, age_num, age, no_books)
            PodkovaFont.REGULAR.apply(status, ar)

            adapter = ShelvesAdapter(this, model.shelves) { shelfId: Int, on: Boolean ->
                model.shelvesSelection.put(shelfId, on)
                if (model.shelvesSelection.size() > 0) {
                    this@run.bottom.visibility = VISIBLE
                    ar.visibility = VISIBLE
                } else {
                    this@run.bottom.visibility = INVISIBLE
                    ar.visibility = INVISIBLE
                }
            }

            shelves.layoutManager = LinearLayoutManager(this@run, RecyclerView.VERTICAL, false)
            shelves.adapter = adapter
            shelves.hasFixedSize()

            // appbar fade when scroll..
            appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, offset ->
                val percentage =
                    (appBarLayout.totalScrollRange - Math.abs(offset).toFloat() * 1.2f) / appBarLayout.totalScrollRange
                header.alpha = percentage
            })

            if (model.processingDone.value == true) {
                // if previously loaded - set all books at once
                adapter.setBooks(model.booksByShelf)
                adapter.selectShelves(model.shelvesSelection)
            } else {
                // if not - start processing books one by one
                model.loadCovers()
                model.processedBook.observe(this, Observer { (shelfId, book) ->
                    adapter.addBook(shelfId, book)
                })
            }

            model.numProcessed.observe(this, Observer {
                status.text = String.format(resources.getString(R.string.processed_n_books), it, model.numBooks)
            })

            model.processingDone.observe(this, Observer { done ->
                if (done) {
                    progress.visibility = INVISIBLE
                    if (model.shelvesSelection.size() > 0) {
                        ar.visibility = VISIBLE
                        bottom.visibility = VISIBLE
                    } else {
                        no_books.visibility = VISIBLE
                        toolbar.visibility = INVISIBLE
                    }
                    adapter.allowSelection = true
                    status.text = resources.getString(R.string.processing_complete)
                }
            })

            ar.setOnClickListener {
                findNavController().navigate(R.id.action_shelves_to_ar)
            }
        }

    }


}
