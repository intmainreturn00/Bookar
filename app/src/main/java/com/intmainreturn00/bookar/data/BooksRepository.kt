package com.intmainreturn00.bookar.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.intmainreturn00.bookar.data.db.BooksDao
import com.intmainreturn00.bookar.data.db.BooksRoomDatabase
import com.intmainreturn00.bookar.data.network.BitmapLoader
import com.intmainreturn00.bookar.domain.*
import com.intmainreturn00.grapi.grapi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


sealed class RepoStatus {
    object Nothing : RepoStatus()
    object LoadingProfile : RepoStatus()
    object LoadingBooks : RepoStatus()
    data class ProcessingBooks(val numProcessed: Int, val numBooks: Int, val lastProcessedBook: Book) : RepoStatus()
}

// Singleton
class BooksRepository private constructor(
    private val context: Context
) {

    companion object : SingletonHolder<BooksRepository, Context>(::BooksRepository)

    private val booksDao: BooksDao = BooksRoomDatabase.getDatabase(context).booksDao()

    private val _status = MutableLiveData<RepoStatus>()
    val status: LiveData<RepoStatus> = _status

    init {
        _status.value = RepoStatus.Nothing
    }

    fun hasUser(): LiveData<Boolean> = Transformations.map(booksDao.countUsers()) { it != 0 }

    fun getUser(): LiveData<User> = booksDao.getUser()

    fun getShelves(): LiveData<List<Shelf>> = booksDao.getShelves()

    suspend fun fetchProfile() {
        _status.value = RepoStatus.LoadingProfile
        withContext(Dispatchers.IO) {
            println("@ get user from network")
            val grUserId = grapi.getUserId()
            val grUser = grapi.getUser(grUserId.id)
            val shelves = grUser.shelves.filter { it.bookCount > 0 }.map { Shelf(it.id.toInt(), it.name) }
            val user = User(grUser.id.toInt(), grUser.name, grUser.username, grUser.imageUrl, grUser.joined)

            println("@ push user and shelves to db")
            booksDao.updateProfile(user, shelves)
        }
        _status.value = RepoStatus.Nothing
    }

    fun hasBooks(): LiveData<Boolean> = Transformations.map(booksDao.countBooks()) { it != 0 }

    fun getBooks(): LiveData<List<Book>> = booksDao.getBooks()


    suspend fun fetchBooks() {
        _status.value = RepoStatus.LoadingBooks
        withContext(Dispatchers.IO) {
            println("@ clearBooks books from db")
            booksDao.clearBookTable()
            println("@ get user sync from db")
            val user = booksDao.getUserSync()
            println("@ $user")
            println("@ get reviews from network")
//            val allReviews = grapi.getReviewList(user.id.toString(), perPage = 50).reviews
//            val allReviews = grapi.getAllReviews(user.id.toString())
            val allReviews = grapi.getAllReviewsConcurrent(user.id.toString())
            val numBooks = allReviews.size
            val numPages = allReviews.sumBy { it.book.numPages ?: 0 }
            println("@ update user with stats to db")
            val newUser = user.copy(numBooks = numBooks, numPages = numPages)
            booksDao.insertUser(newUser)

            val books = mutableListOf<Book>()
            allReviews.forEachIndexed { index, review ->
                val book = BookFactory.createFromReview(
                    review,
                    ResourceProvider.getInstance(context),
                    BitmapLoader.getInstance(context)
                )
                books.add(book)
                _status.postValue(RepoStatus.ProcessingBooks(index + 1, numBooks, book))
            }
            booksDao.insertBooks(books)
        }
        _status.value = RepoStatus.Nothing
    }


    suspend fun refresh() {
        fetchProfile()
        fetchBooks()
    }

}


