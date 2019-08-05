package com.intmainreturn00.bookar.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val id: Int,
    val name: String,
    val username: String,
    val avatar: String,
    val joined: String,
    val numPages: Int? = null,
    val numBooks: Int? = null
) {
    override fun hashCode(): Int = id
}

@Entity(tableName = "stat_table")
data class Stat(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val numBooks: Int?,
    val numPages: Int?,
    val joined: String
)

@Entity(tableName = "shelf_table")
data class Shelf(@PrimaryKey val id: Int, val name: String)

@Entity(tableName = "book_table")
data class Book(
    @PrimaryKey val id: Int,
    val title: String,
    val author: String,
    val pages: Int?,
    val rating: Int?,
    val readCount: Int?,
    val shelves: List<Shelf>,
    val cover: Cover
) {
    override fun hashCode(): Int = id
}

sealed class Cover {
    abstract val width: Int
    abstract val height: Int
    abstract val spineColor: Int

    companion object {
        const val TypeImage = 0
        const val TypeTemplate = 1
        fun toInt(cover: Cover) = when (cover) {
            is ImageCover -> TypeImage
            is TemplateCover -> TypeTemplate
        }
    }
}

data class ImageCover(
    override val width: Int,
    override val height: Int,
    override val spineColor: Int,
    val url: String
) : Cover()

data class TemplateCover(
    override val width: Int,
    override val height: Int,
    override val spineColor: Int,
    val textColor: Int
) : Cover()

// ARBook - allocate-ed Book =)
data class ARBook(
    val size: MyVector3,
    val position: MyVector3,
    val rotation: MyQuaternion,
    val bookModel: Book,
    var isTopBook: Boolean = false
)

data class MyQuaternion(val x: Float, val y: Float, val z: Float, val w: Float)
data class MyVector3(val x: Float, val y: Float, val z: Float)
