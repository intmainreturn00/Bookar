package com.intmainreturn00.arbooks.viewmodels

import android.app.Application
import android.util.SparseArray
import android.util.SparseBooleanArray
import androidx.core.util.forEach
import androidx.lifecycle.*
import com.intmainreturn00.arbooks.domain.*
import com.intmainreturn00.grapi.Review
import com.intmainreturn00.grapi.grapi
import kotlinx.coroutines.launch


class BooksViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var user: User
    private lateinit var allReviews: List<Review>
    lateinit var shelves: List<Shelf>
    var numBooks = 0
    var numPages = 0
    val booksLoadingDone = MutableLiveData<Boolean>()

    val booksByShelf = SparseArray<MutableList<Book>>() // shelfId -> books
    private val uniqueBookIds = HashSet<Int>()
    val processedBook = MutableLiveData<Pair<Int, Book>>() // <shelfId, books>
    var numProcessed = MutableLiveData<Int>()
    val processingDone = MutableLiveData<Boolean>()
    val shelvesSelection = SparseBooleanArray()


    val arbooks = MutableLiveData<List<ARBook>>()

    init {
        numProcessed.value = 0
        processingDone.value = false
    }

    fun loadBooks() {
        viewModelScope.launch {
            val grUserId = grapi.getUserId()
            val grUser = grapi.getUser(grUserId.id)
            shelves = grUser.shelves.filter { it.bookCount > 0 }.map { Shelf(it.id.toInt(), it.name) }
            user = User(grUser.id.toInt(), grUser.name, grUser.username, grUser.imageUrl, grUser.joined)
            //allReviews = grapi.getAllReviews(user.id.toString())
            allReviews = grapi.getReviewList(user.id.toString(), perPage = 25).reviews // DEV
            numBooks = allReviews.size
            numPages = allReviews.sumBy { it.book.numPages ?: 0 }
            booksLoadingDone.value = true
        }
    }

    fun loadCovers() {

        viewModelScope.launch {
            uniqueBookIds.clear()
            shelves.forEach { booksByShelf.put(it.id, mutableListOf()) }

            allReviews.forEach { review ->
                if (!uniqueBookIds.contains(review.book.id.toInt())) {
                    uniqueBookIds.add(review.book.id.toInt())
                    numProcessed.value = numProcessed.value!! + 1
                }
                val book = BookFactory.createFromReview(review, getApplication())
                review.shelves.forEach { bookShelf ->
                    booksByShelf[bookShelf.id.toInt()]!!.add(book)
                    processedBook.value = Pair(bookShelf.id.toInt(), book)
                }
            }
            // select all shelves
            shelves.forEach { shelvesSelection.put(it.id, true) }
            processingDone.value = true
            println("@")
        }
    }

    fun allocateBooksInAR(type: AllocationType) {
        viewModelScope.launch {
            val selectedBooks = mutableListOf<Book>()
            booksByShelf.forEach { shelfId, books ->
                if (shelvesSelection[shelfId]) {
                    selectedBooks.addAll(books)
                }
            }

            val sortedBooks = selectedBooks
                .distinct()
                .sortedWith(compareBy({ it.readCount }, { it.rating }))

            arbooks.value = Allocator.getInstance(type).allocate(sortedBooks)
        }
    }

    fun shuffle(type: AllocationType) {
        viewModelScope.launch {
            val selectedBooks = mutableListOf<Book>()
            booksByShelf.forEach { shelfId, books ->
                if (shelvesSelection[shelfId]) {
                    selectedBooks.addAll(books)
                }
            }

            val sortedBooks = selectedBooks
                .distinct()
                .shuffled()

            arbooks.value = Allocator.getInstance(type).allocate(sortedBooks)
        }
    }

}