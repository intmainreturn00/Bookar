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
    val reviews = HashMap<String, List<BookModel>>() // shelf -> books

    val currentLoadingShelf = MutableLiveData<String>()
    val booksLoadingDone = MutableLiveData<Boolean>()

    var numBooks = 0
    var numPages = 0

    val lastProcessedBook = MutableLiveData<BookModel>()
    var shelfIndex = -1
    var shelfTitle = ""
    var numProcessed = MutableLiveData<Int>()

    val processingDone = MutableLiveData<Boolean>()

    init {
        numProcessed.value = 0
    }


    fun loadBooks() {
        viewModelScope.launch {
            userId = grapi.getUserId()
            user = grapi.getUser(userId.id)
            shelves = grapi.getAllShelves(userId.id)//.takeLast(1)
            //.filterIndexed { index, _ -> (index == 1 || index == 3 || index == 2) }
            //.takeLast(2)


            for (shelf in shelves) {
                currentLoadingShelf.value = shelf.name
                val currentReviews = grapi.getAllReviews(userId.id, shelf.name)

                val bookModels = mutableListOf<BookModel>()
                numBooks += currentReviews.size
                currentReviews.forEach { review ->
                    numPages += review.book.numPages ?: 0
                    bookModels.add(constructFromReview(review))
                }
                reviews[shelf.name] = bookModels
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
                    numProcessed.value = numProcessed.value!! + 1

                    downloadImage(getApplication(), book.cover).let {
                        if (it != null) {
                            book.coverColor = ColorArt(it).backgroundColor
                        } else {
                            book.cover = ""
                        }
                    }

                    lastProcessedBook.value = book
                }
            }
            processingDone.value = true
        }
    }

}