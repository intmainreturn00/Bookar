package com.intmainreturn00.arbooks

import android.app.Application
import androidx.lifecycle.*
import com.intmainreturn00.grapi.*
import kotlinx.coroutines.launch

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

class BooksViewModel(application: Application) : AndroidViewModel(application) {

    val userId = MutableLiveData<UserId>()
    val user = MutableLiveData<User>()
    val shelves = MutableLiveData<List<Shelf>>()
    val reviews = MutableLiveData<MutableMap<String, List<Review>>>()

    val currentShelf = MutableLiveData<String>()
    val loadingDone = MutableLiveData<Boolean>()

    init {
        reviews.value = HashMap()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            userId.value = grapi.getUserId()
            user.value = grapi.getUser(userId.value!!.id)
            downloadImage(getApplication(), user.value?.imageUrl ?: "")
            shelves.value = grapi.getAllShelves(userId.value!!.id)

            for (shelf in shelves.value!!) {
                currentShelf.value = shelf.name
                reviews.value?.put(shelf.name, grapi.getAllReviews(userId.value!!.id, shelf.name))
                reviews.notifyObserver()
                println("@ books from ${shelf.name} added")
            }

            loadingDone.value = true
        }
    }

}