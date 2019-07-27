package com.intmainreturn00.arbooks.domain


data class User(
    val id: Int,
    val name: String,
    val username: String,
    val avatar: String,
    val joined: String
) {
    override fun hashCode(): Int = id
}

data class Shelf(val id: Int, val name: String)

data class Book(
    val id: Int,
    val pages: Int?,
    val rating: Int?,
    val readCount: Int?,
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
    val textColor: Int,
    val title: String,
    val author: String
) : Cover()

// ARBook - allocate-ed Book =)
data class ARBook(
    val size: MyVector3,
    val position: MyVector3,
    val rotation: MyQuaternion,
    val bookModel: Book
)

data class MyQuaternion(val x: Float, val y: Float, val z: Float, val w: Float)
data class MyVector3(val x: Float, val y: Float, val z: Float)
