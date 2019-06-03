package com.intmainreturn00.arbooks

import android.graphics.Color
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.intmainreturn00.grapi.grapi
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.browse
import org.jetbrains.anko.startActivity

class LoginActivity : ScopedAppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (grapi.isLoggedIn()) {
            // already logged in
            loadData()
        } else {
            if (intent.data == null) {
                // regular launch - not logged in
                login.visibility = VISIBLE
                intro_login.visibility = VISIBLE
            } else {
                // auth redirect
                login.visibility = INVISIBLE
                intro_login.visibility = INVISIBLE

                launch {
                    grapi.loginEnd(intent) { ok ->
                        if (ok) {
                            loadData()
                        }
                    }
                }
            }

            login.setOnClickListener {
                launch {
                    grapi.loginStart()
                    browse(grapi.getAuthorizationUrl())
                }
            }
        }

    }




    private fun loadData() {
        launch {
            login.visibility = INVISIBLE
            intro_login.visibility = INVISIBLE
            loading.visibility = VISIBLE
            progress.visibility = VISIBLE

            val userId = grapi.getUserId()
            val bookModels = mutableListOf<BookModel>()
            val reviews = grapi.getAllReviews(userId.id/*, sort = Sort.DATE_ADDED, order = Order.ASCENDING*/)

            progress.visibility = INVISIBLE
            loading_done.text = loading_done.text.toString().replace(
                "#",
                resources.getQuantityString(R.plurals.books, reviews.size, reviews.size))
            loading_done.visibility = VISIBLE
            loading.setTextColor(Color.GRAY)

            if (reviews.isEmpty()) {
                loading_done.text = loading_done.text.toString() + " " + resources.getString(R.string.sorry)
                // we can stop here
                return@launch
            } else {
                loading_done.text = loading_done.text.toString() + " " + resources.getString(R.string.loading_covers)
            }

            images.visibility = VISIBLE

            withContext(Dispatchers.Default) {
                val sortedReviews = reviews.sortedWith(
                    compareBy(
                        { it.readCount },
                        { it.rating }
                    )
                )//.takeLast(17)

                for (review in sortedReviews) {
                    App.totalPages += review.book.numPages ?: 0
                    when {
                        !review.book.imageUrl.contains("nophoto") ->
                            bookModels.add(BookModel(review.book.numPages, review.book.imageUrl))

                        review.book.isbn.isNotEmpty() ->
                            bookModels.add(BookModel(review.book.numPages, makeOpenlibLink(review.book.isbn)))

                        else -> bookModels.add(BookModel(review.book.numPages, ""))
                    }
                }

                App.books = makeGrid(bookModels)
            }

            cover_progress.visibility = VISIBLE
            var total = 1
            var i = 0
            prefetchCovers(this@LoginActivity, App.books) { btm, _ ->
                cover_progress.progress = total++ * 100 / reviews.size
                btm?.let {
                    when(i) {
                        0 -> {
                            img1.visibility = VISIBLE
                            img1.setImageBitmap(it)
                        }
                        1 -> {
                            img2.visibility = VISIBLE
                            img2.setImageBitmap(it)
                        }
                        2 -> {
                            img3.visibility = VISIBLE
                            img3.setImageBitmap(it)
                        }
                    }
                    if (++i == 3) i = 0
                }
            }

            ready.visibility = VISIBLE
            ar.show()
            ar.setOnClickListener {
                startActivity<ARActivity>()
            }
        }
    }
}
