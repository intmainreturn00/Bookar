package com.intmainreturn00.arbooks

import android.app.Application
import androidx.lifecycle.*
import com.intmainreturn00.grapi.*
import kotlinx.coroutines.launch

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

class BooksViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var userId: UserId
    lateinit var user: User
    lateinit var shelves: List<Shelf>
    val reviews = HashMap<String, List<Review>>()

    val currentLoadingShelf = MutableLiveData<String>()
    val booksLoadingDone = MutableLiveData<Boolean>()

    var numBooks = 0
    var numPages = 0


    fun loadProfileData() {
        viewModelScope.launch {
            userId = grapi.getUserId()
            user = grapi.getUser(userId.id)
            shelves = grapi.getAllShelves(userId.id).takeLast(1)

            for (shelf in shelves) {
                currentLoadingShelf.value = shelf.name
                val currentReviews = grapi.getAllReviews(userId.id, shelf.name)
                currentReviews.forEach {
                    numBooks += 1
                    numPages += it.book.numPages ?: 0
                }
                reviews[shelf.name] = currentReviews
                println("@ books from ${shelf.name} added")
            }


            booksLoadingDone.value = true
        }
    }

}