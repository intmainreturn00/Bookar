package com.intmainreturn00.arbooks

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
            } else {
                // auth redirect
                login.visibility = INVISIBLE

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
            progress.visibility = VISIBLE

            val userId = grapi.getUserId()
            val bookModels = mutableListOf<BookModel>()
            val reviews = grapi.getAllReviews(userId.id/*, sort = Sort.DATE_ADDED, order = Order.ASCENDING*/)

            withContext(Dispatchers.Default) {
                val sortedReviews = reviews.sortedWith(
                    compareBy(
                        { it.readCount },
                        { it.rating }
                    )
                )//.takeLast(17)

                for (review in sortedReviews) {
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

            prefetchCovers(this@LoginActivity, App.books)

            progress.visibility = INVISIBLE

            ar.show()
            ar.setOnClickListener {
                startActivity<ARActivity>()
            }
        }
    }
}
