package com.intmainreturn00.bookar.domain

import android.content.Context
import com.intmainreturn00.bookar.data.network.downloadImage
import com.intmainreturn00.grapi.Author
import com.intmainreturn00.grapi.Review
import org.michaelevans.colorart.library.ColorArt

interface BookFactory {
    companion object {

        suspend fun createFromReview(review: Review, resourceProvider: ResourceProvider, ctx: Context): Book {
            val url = when {
                !review.book.imageUrl.contains("nophoto") -> review.book.imageUrl
                review.book.isbn.isNotEmpty() -> makeOpenlibLink(review.book.isbn)
                else -> ""
            }

            downloadImage(ctx, url).let {
                return if (it != null) {
                    val spineColor = ColorArt(it).backgroundColor
                    val c = ImageCover(it.width, it.height, spineColor, url)
                    makeBookWithCover(review, c)
                } else {
                    val width = resourceProvider.templateWidth
                    val height = resourceProvider.templateHeight
                    val (spineColor, textColor) = resourceProvider.makeRandomTemplateColors()
                    val c = TemplateCover(width, height, spineColor, textColor)
                    makeBookWithCover(review, c)
                }
            }
        }

        private fun makeBookWithCover(review: Review, c: Cover): Book = Book(
            id = review.book.id.toInt(),
            title = review.book.titleWithoutSeries,
            author = trimAuthorList(review.book.authors),
            pages = review.book.numPages,
            rating = review.rating,
            readCount = review.readCount,
            shelves = review.shelves.map { Shelf(it.id.toInt(), it.name) },
            cover = c
        )


        private fun trimAuthorList(authors: List<Author>): String =
            if (authors.isNotEmpty()) {
                authors[0].name
            } else {
                ""
            }

        private fun makeOpenlibLink(isbn: String) = "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg"

    }
}