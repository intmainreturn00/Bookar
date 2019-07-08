package com.intmainreturn00.arbooks

import android.app.Application
import androidx.lifecycle.*
import com.intmainreturn00.grapi.*
import kotlinx.coroutines.launch
import org.michaelevans.colorart.library.ColorArt
import kotlin.random.Random

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

class BooksViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var userId: UserId
    lateinit var user: User
    lateinit var shelves: List<Shelf>
    val bookModelsByShelf = HashMap<String, List<BookModel>>() // shelf_name -> books
    private val uniqueBookIds = HashSet<Int>()

    val currentLoadingShelf = MutableLiveData<String>()
    val booksLoadingDone = MutableLiveData<Boolean>()

    var numBooks = 0
    var numPages = 0

    val lastProcessedBook = MutableLiveData<BookModel>()
    var shelfIndex = -1
    var shelfTitle = ""
    var numProcessed = MutableLiveData<Int>()

    val processingDone = MutableLiveData<Boolean>()
    val selectedShelves = HashSet<String>()

    val arbooks = MutableLiveData<List<ARBook>>()

    init {
        numProcessed.value = 0
        processingDone.value = false
    }

    fun loadBooks() {
        viewModelScope.launch {
            userId = grapi.getUserId()
            user = grapi.getUser(userId.id)
            shelves = grapi.getAllShelves(userId.id).takeLast(1)
            //.filterIndexed { index, _ -> (index == 1 || index == 3 || index == 2) }
            //.takeLast(2)


            for (shelf in shelves) {
                currentLoadingShelf.value = shelf.name
                val currentReviews = grapi.getAllReviews(userId.id, shelf.name).takeLast(5)

                val bookModels = mutableListOf<BookModel>()
                currentReviews.forEach { review ->
                    if (!uniqueBookIds.contains(review.book.id.toInt())) {
                        uniqueBookIds.add(review.book.id.toInt())
                        numBooks += 1
                        numPages += review.book.numPages ?: 0
                    }
                    bookModels.add(constructFromReview(review))
                }
                bookModelsByShelf[shelf.name] = bookModels.asReversed()
            }

            booksLoadingDone.value = true
        }
    }


    fun loadCovers() {
        viewModelScope.launch {
            uniqueBookIds.clear()
            bookModelsByShelf.forEach { (title, books) ->
                // for each shelf..
                this@BooksViewModel.shelfTitle = title
                shelfIndex++
                books.forEach { book ->
                    // for each book on shelf..
                    if (!uniqueBookIds.contains(book.id)) {
                        uniqueBookIds.add(book.id)
                        numProcessed.value = numProcessed.value!! + 1
                    }

                    downloadImage(getApplication(), book.cover).let {
                        if (it != null) {
                            book.coverType = CoverType.COVER
                            book.coverWidth = it.width
                            book.coverHeight = it.height
                            book.coverColor = ColorArt(it).backgroundColor
                        } else {
                            book.cover = ""
                            book.coverType = CoverType.makeRandomTemplate()
                            book.coverWidth = dpToPix(getApplication<App>(), 75f).toInt()
                            book.coverHeight = dpToPix(getApplication<App>(), 120f).toInt()
                            book.coverColor = coverBackgroundColor(getApplication<App>(), book.coverType)
                            book.textColor = coverTextColor(getApplication<App>(), book.coverType)
                        }
                    }

                    lastProcessedBook.value = book
                }
            }
            selectedShelves.addAll(shelves.map { it.name })
            processingDone.value = true
        }
    }


    fun moveBooksToAR(type: PLACEMENT) {
        viewModelScope.launch {

            val bookModels = bookModelsByShelf
                .filter { selectedShelves.contains(it.key) }
                .flatMap { it.value }
                .distinct()
                .sortedWith(compareBy({ it.readCount }, { it.rating }))

            if (type == PLACEMENT.GRID) {
                arbooks.value = makeGrid(bookModels)
            } else {
                arbooks.value = makeTower(bookModels)
            }
        }
    }

    fun shuffle(type: PLACEMENT) {
        viewModelScope.launch {

            val bookModels = bookModelsByShelf
                .filter { selectedShelves.contains(it.key) }
                .flatMap { it.value }
                .distinct()
                .shuffled()

            if (type == PLACEMENT.GRID) {
                arbooks.value = makeGrid(bookModels)
            } else {
                arbooks.value = makeTower(bookModels)
            }
        }
    }

}