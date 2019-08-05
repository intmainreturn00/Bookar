package com.intmainreturn00.bookar.viewmodels

import android.app.Application
import android.util.SparseBooleanArray
import androidx.core.util.forEach
import androidx.lifecycle.*
import com.intmainreturn00.bookar.data.BooksRepository
import com.intmainreturn00.bookar.data.RepoStatus
import com.intmainreturn00.bookar.domain.*
import kotlinx.coroutines.launch


fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

class BooksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BooksRepository = BooksRepository.getInstance(application)

    val repoStatus: LiveData<RepoStatus>
    val hasProfileCache: LiveData<Boolean>
    val user: LiveData<User>
    val shelves: LiveData<List<Shelf>>
    val hasBooksCache: LiveData<Boolean>
    val books: LiveData<List<Book>>

    private val selectedShelves = SparseBooleanArray()

    fun getSelectedShelves() = selectedShelves.clone()

    val selectedBooks = mutableListOf<Book>()

    fun selectShelf(shelfId: Int, on: Boolean) {
        selectedShelves.put(shelfId, on)
        selectedBooks.clear()
    }

    val arbooks = MutableLiveData<List<ARBook>>()

    init {
        repoStatus = repository.status
        user = repository.getUser()
        shelves = repository.getShelves()
        hasProfileCache = repository.hasUser()
        hasBooksCache = repository.hasBooks()
        books = repository.getBooks()

        shelves.observeForever { shelves ->
            selectedShelves.clear()
            shelves.forEach { selectedShelves.put(it.id, true) }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            repository.fetchProfile()
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            repository.fetchBooks()
        }
    }

    fun isSomethingSelected(): Boolean {
        selectedShelves.forEach { _, isSelected ->
            if (isSelected)
                return true
        }
        return false
    }

    fun isExactlyOneShelfSelected(): String? {
        var selectedNum = 0
        var selectedId = 0
        selectedShelves.forEach { shelfId, isSelected ->
            if (isSelected) {
                ++selectedNum
                if (selectedNum > 1)
                    return null
                selectedId = shelfId
            }
        }
        return if (selectedNum == 1) {
            shelves.value?.find { it.id == selectedId }?.name
        } else {
            null
        }
    }

    fun allocateBooksInAR(type: AllocationType) {
        viewModelScope.launch {
            if (selectedBooks.isEmpty()) {
                // sort && selection - one time per multiple allocations
                books.value?.forEach { book ->
                    val isSelected = book.shelves.firstOrNull { selectedShelves.get(it.id) }
                    if (isSelected != null)
                        selectedBooks.add(book)
                }
                selectedBooks.addAll(
                    selectedBooks.sortedWith(compareBy({ it.readCount }, { it.rating }))
                )
            }
            arbooks.value = Allocator.getInstance(type).allocate(selectedBooks)
        }

    }

    fun shuffle(type: AllocationType) {
        viewModelScope.launch {
            val shuffled = selectedBooks.shuffled()
            arbooks.value = Allocator.getInstance(type).allocate(shuffled)
        }
    }

}