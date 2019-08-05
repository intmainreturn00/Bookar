package com.intmainreturn00.bookar.data.db

import android.content.Context
import androidx.room.*
import com.intmainreturn00.bookar.domain.Book
import com.intmainreturn00.bookar.domain.Shelf
import com.intmainreturn00.bookar.domain.User

@Database(entities = arrayOf(User::class, Shelf::class, Book::class), version = 4)
@TypeConverters(Converters::class)
public abstract class BooksRoomDatabase : RoomDatabase() {

    abstract fun booksDao(): BooksDao

    companion object {
        @Volatile
        private var INSTANCE: BooksRoomDatabase? = null

        fun getDatabase(context: Context): BooksRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BooksRoomDatabase::class.java,
                    "Books_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}