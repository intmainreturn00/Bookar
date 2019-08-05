package com.intmainreturn00.bookar.ui.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.intmainreturn00.bookar.*
import com.intmainreturn00.bookar.data.RepoStatus
import com.intmainreturn00.bookar.domain.Book
import com.intmainreturn00.bookar.domain.User
import com.intmainreturn00.bookar.platform.GlideApp
import com.intmainreturn00.bookar.ui.PodkovaFont
import com.intmainreturn00.bookar.ui.ShelvesAdapter
import com.intmainreturn00.bookar.ui.formatProfileAge
import com.intmainreturn00.bookar.viewmodels.BooksViewModel
import com.intmainreturn00.bookar.viewmodels.observeOnce

import kotlinx.android.synthetic.main.fragment_shelves.*

class ShelvesFragment : Fragment() {
    private val model by activityViewModels<BooksViewModel>()
    private lateinit var adapter: ShelvesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shelves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PodkovaFont.EXTRA_BOLD.apply(books_num, books, pages_num, pages, age_num, age, no_books)
        PodkovaFont.REGULAR.apply(status, ar)

        model.hasBooksCache.observeOnce(this, Observer { cached ->
            if (!cached) {
                println("@ fragment request books loading")
                model.loadBooks()
            }
        })

        model.user.observe(this, Observer { user ->
            println("@ $user")
            GlideApp.with(this).load(user.avatar).apply(RequestOptions().circleCrop()).into(avatar)
            updateStatistics(user)
        })

        refresh.setOnClickListener {
            println("@ refresh")
            allowRefresh(false)
            model.refreshData()
        }

        model.repoStatus.observe(this, Observer { repoStatus ->
            val notUsed = when (repoStatus) {
                is RepoStatus.LoadingProfile -> {
                    progress(true)
                    adapter.allowSelection = false
                    allowRefresh(false)
                    allowAR(false)
                    status.visibility = VISIBLE
                    status.text = resources.getString(R.string.loading_profile)
                }
                is RepoStatus.LoadingBooks -> {
                    progress(true)
                    adapter.allowSelection = false
                    allowRefresh(false)
                    allowAR(false)
                    status.visibility = VISIBLE
                    status.text = resources.getString(R.string.loading_books)
                }
                is RepoStatus.ProcessingBooks -> {
                    progress(true)
                    adapter.allowSelection = false
                    allowRefresh(false)
                    allowAR(false)
                    status.visibility = VISIBLE
                    status.text = String.format(
                        resources.getString(R.string.processed_n_books),
                        repoStatus.numProcessed, repoStatus.numBooks
                    )
                    adapter.addBook(repoStatus.lastProcessedBook)
                }
                is RepoStatus.Nothing -> {
                    progress(false)
                    allowRefresh(true)
                    status.visibility = INVISIBLE
                }
            }

        })


        model.shelves.observe(this, Observer {
            println("@ shelves ${it.size}")
            adapter = ShelvesAdapter(activity!!, it) { shelfId: Int, on: Boolean ->
                model.selectShelf(shelfId, on)
                allowAR(model.isSomethingSelected())
            }
            adapter.setHasStableIds(true)
            shelves.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
            shelves.adapter = adapter
            //(shelves.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            shelves.itemAnimator = null
            shelves.hasFixedSize()

            // assure that books arrive after adapter initialized with shelves
            model.books.observe(this, booksObserver)
        })


        // appbar fade when scroll..
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, offset ->
            val percentage =
                (appBarLayout.totalScrollRange - Math.abs(offset).toFloat() * 1.2f) / appBarLayout.totalScrollRange
            header.alpha = percentage
        })

        ar.setOnClickListener {
            findNavController().navigate(R.id.action_shelves_to_ar)
        }

    }

    override fun onPause() {
        super.onPause()
        // assure that books arrive after adapter initialized with shelves
        model.books.removeObserver(booksObserver)
    }

    private val booksObserver = Observer<List<Book>> { books ->
        println("@ books update = ${books.size}")
        adapter.setBooks(books)
        adapter.selectShelves(model.getSelectedShelves())
        adapter.allowSelection = books.isNotEmpty()
        allowAR(books.isNotEmpty())
    }

    private fun progress(on: Boolean) {
        if (on) {
            progress.visibility = VISIBLE
        } else {
            progress.visibility = INVISIBLE
        }
    }

    private fun allowRefresh(on: Boolean) {
        if (on) {
            refresh.visibility = VISIBLE
        } else {
            refresh.visibility = INVISIBLE
        }
    }

    private fun allowAR(on: Boolean) {
        if (on) {
            ar.visibility = VISIBLE
            bottom.visibility = VISIBLE
        } else {
            ar.visibility = INVISIBLE
            bottom.visibility = INVISIBLE
        }
    }

    private fun updateStatistics(user: User) {
        if (user.numBooks != null && user.numPages != null) {
            books_num.text = user.numBooks.toString()
            books.text = resources.getQuantityString(R.plurals.books, user.numBooks)
            books_num.visibility = VISIBLE
            books.visibility = VISIBLE

            pages_num.text = user.numPages.toString()
            pages.text = resources.getQuantityString(R.plurals.pages, user.numPages)
            pages_num.visibility = VISIBLE
            pages.visibility = VISIBLE

            age_num.visibility = VISIBLE
            age.visibility = VISIBLE
            val (num, qualifier) = formatProfileAge(user.joined)
            age_num.text = num
            age.text = qualifier
        } else {
            books_num.visibility = INVISIBLE
            books.visibility = INVISIBLE
            pages_num.visibility = INVISIBLE
            pages.visibility = INVISIBLE
            age_num.visibility = INVISIBLE
            age.visibility = INVISIBLE
        }
    }

}
