package com.intmainreturn00.arbooks.fragments


import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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

            PodkovaFont.EXTRA_BOLD.apply(books_num, books, pages_num, pages, age_num, age)
            PodkovaFont.REGULAR.apply(status, ar)

            shelves.apply {
                layoutManager = LinearLayoutManager(this@run, RecyclerView.VERTICAL, false)
                adapter = ShelvesAdapter(this@run, shelvesModels) { on: Boolean, position: Int ->

                }
            }

            appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, offset ->
                val percentage =
                    (appBarLayout.totalScrollRange - Math.abs(offset).toFloat() * 1.2f) / appBarLayout.totalScrollRange

                header.alpha = percentage
            })

            model.loadCovers()

            model.lastLoadedCover.observe(this, Observer {
                val index = model.shelfIndex
                if (index >= shelvesModels.size) {
                    shelvesModels.add(ShelfModel(model.shelfTitle, mutableListOf()))
                    (shelves.adapter as ShelvesAdapter).addShelf()
                    (shelves.adapter as ShelvesAdapter).notifyDataSetChanged()
                }
                shelvesModels[index].images.add(0, it)
                (shelves.adapter as ShelvesAdapter).notifyBookAdded(index)
            })

            model.numLoaded.observe(this, Observer {
                status.text = String.format(resources.getString(R.string.processed_n_books), it, model.numBooks)
            })

            model.coversLoadingDone.observe(this, Observer { done ->
                if (done) {
                    progress.visibility = INVISIBLE
                    ar.visibility = VISIBLE
                    (shelves.adapter as ShelvesAdapter).allowEditing = true
                    (shelves.adapter as ShelvesAdapter).notifyDataSetChanged()
                    status.text = resources.getString(R.string.processing_done)
                }
            })
        }

    }


}
