package com.intmainreturn00.arbooks.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.intmainreturn00.arbooks.*

import kotlinx.android.synthetic.main.fragment_shelves.*

class ShelvesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shelves, container, false)
    }

    private val shelvesModels = mutableListOf<ShelfModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.run {
            val model = ViewModelProviders.of(this).get(BooksViewModel::class.java)
            GlideApp.with(this).load(model.user.imageUrl).apply(RequestOptions().circleCrop()).into(avatar)

            books_num.text = model.numBooks.toString()
            books.text = resources.getQuantityString(R.plurals.books, model.numBooks)
            pages_num.text = model.numPages.toString()
            pages.text = resources.getQuantityString(R.plurals.pages, model.numPages)
            val (num, qualifier) = formatProfileAge(model.user.joined)
            age_num.text = num
            age.text = qualifier

            PodkovaFont.EXTRA_BOLD.apply(books_num, books, pages_num, pages, age_num, age, no_books)
            PodkovaFont.REGULAR.apply(status, ar)

            shelves.apply {
                layoutManager = LinearLayoutManager(this@run, RecyclerView.VERTICAL, false)
                adapter = ShelvesAdapter(this@run, shelvesModels) { on: Boolean, position: Int ->
                    if (on) {
                        model.selectedShelves.add(position)
                    } else {
                        model.selectedShelves.remove(position)
                    }
                    if (model.selectedShelves.size > 0) {
                        ar.visibility = VISIBLE
                    } else {
                        ar.visibility = INVISIBLE
                    }
                }
            }

            appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, offset ->
                val percentage =
                    (appBarLayout.totalScrollRange - Math.abs(offset).toFloat() * 1.2f) / appBarLayout.totalScrollRange

                header.alpha = percentage
            })

            model.loadCovers()

            model.lastProcessedBook.observe(this, Observer {
                val index = model.shelfIndex
                if (index >= shelvesModels.size) {
                    shelvesModels.add(ShelfModel(index, model.shelfTitle, mutableListOf()))
                    (shelves.adapter as ShelvesAdapter).addShelf()
                }
                shelvesModels[index].books.add(0, it)
                (shelves.adapter as ShelvesAdapter).notifyBookAdded(index)
            })

            model.numProcessed.observe(this, Observer {
                status.text = String.format(resources.getString(R.string.processed_n_books), it, model.numBooks)
            })

            model.processingDone.observe(this, Observer { done ->
                if (done) {
                    progress.visibility = INVISIBLE
                    if (model.selectedShelves.size > 0) {
                        ar.visibility = VISIBLE
                    } else {
                        no_books.visibility = VISIBLE
                        toolbar.visibility = INVISIBLE
                    }
                    (shelves.adapter as ShelvesAdapter).lock = false
                    (shelves.adapter as ShelvesAdapter).notifyItemRangeChanged(
                        0,
                        (shelves.adapter as ShelvesAdapter).itemCount
                    )
                    status.text = resources.getString(R.string.processing_complete)
                    bottom.visibility = VISIBLE
                }
            })
        }

    }


}
