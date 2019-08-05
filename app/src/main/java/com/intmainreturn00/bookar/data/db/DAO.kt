package com.intmainreturn00.bookar.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intmainreturn00.bookar.domain.Book
import com.intmainreturn00.bookar.domain.Shelf
import com.intmainreturn00.bookar.domain.User

@Dao
interface BooksDao {

    // user_table

    @Query("SELECT * from user_table LIMIT 1")
    fun getUser(): LiveData<User>

    @Query("SELECT * from user_table LIMIT 1")
    suspend fun getUserSync(): User

    @Query("SELECT COUNT(*) from user_table")
    fun countUsers(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM user_table")
    suspend fun clearUserTable()


    // shelf_table

    @Query("SELECT * from shelf_table")
    fun getShelves(): LiveData<List<Shelf>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelves(shelves: List<Shelf>)

    @Query("DELETE FROM shelf_table")
    suspend fun clearShelfTable()


    // book_table

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Query("SELECT * from book_table")
    fun getBooks(): LiveData<List<Book>>

    @Query("DELETE FROM book_table")
    suspend fun clearBookTable()

    @Query("SELECT COUNT(*) from book_table")
    fun countBooks(): LiveData<Int>

    // combined

    @Transaction
    suspend fun clearAll() {
        clearUserTable()
        clearShelfTable()
        clearBookTable()
    }

    @Transaction
    suspend fun updateProfile(user: User, shelves: List<Shelf>) {
        clearAll()
        insertUser(user)
        insertShelves(shelves)
    }
}