package com.intmainreturn00.arbooks

import android.app.Application
import androidx.lifecycle.*
import com.intmainreturn00.grapi.*
import kotlinx.coroutines.launch
import org.michaelevans.colorart.library.ColorArt

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

class BooksViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var userId: UserId
    lateinit var user: User
    lateinit var shelves: List<Shelf>
    val reviews = HashMap<String, List<BookModel>>()

    val currentLoadingShelf = MutableLiveData<String>()
    val booksLoadingDone = MutableLiveData<Boolean>()

    var numBooks = 0
    var numPages = 0

    val lastLoadedCover = MutableLiveData<String>()
    var shelfIndex = -1
    var shelfTitle = ""
    var numLoaded = MutableLiveData<Int>()

    val coversLoadingDone = MutableLiveData<Boolean>()

    init {
        numLoaded.value = 0
    }

    fun loadBooks() {
        viewModelScope.launch {
            userId = grapi.getUserId()
            user = grapi.getUser(userId.id)
            shelves = grapi.getAllShelves(userId.id)
            //.filterIndexed { index, _ -> (index == 1 || index == 3 || index == 2) }
            //.takeLast(2)
            //


            for (shelf in shelves) {
                currentLoadingShelf.value = shelf.name
                val currentReviews = grapi.getAllReviews(userId.id, shelf.name)

                val sortedReviews = currentReviews.sortedWith(
                    compareBy(
                        { it.readCount },
                        { it.rating }
                    )
                )

                val bookModels = mutableListOf<BookModel>()
                numBooks += sortedReviews.size
                sortedReviews.forEach { review ->
                    numPages += review.book.numPages ?: 0

                    when {
                        !review.book.imageUrl.contains("nophoto") ->
                            bookModels.add(BookModel(review.book.numPages, review.book.imageUrl))

                        review.book.isbn.isNotEmpty() ->
                            bookModels.add(BookModel(review.book.numPages, makeOpenlibLink(review.book.isbn)))

                        else -> bookModels.add(BookModel(review.book.numPages, ""))
                    }

                }
                reviews[shelf.name] = bookModels
                println("@ books from ${shelf.name} added")
            }


            booksLoadingDone.value = true
        }
    }

    fun loadCovers() {
        viewModelScope.launch {
            reviews.forEach { (title, books) ->
                // for each shelf..
                this@BooksViewModel.shelfTitle = title
                shelfIndex++
                books.forEach { book ->
                    // for each book on shelf..
                    numLoaded.value = numLoaded.value!! + 1
                    downloadImage(getApplication(), book.cover)?.let {
                        println(ColorArt(it).backgroundColor)
                        lastLoadedCover.value = book.cover
                    }
                }
            }
            coversLoadingDone.value = true
        }
    }

}