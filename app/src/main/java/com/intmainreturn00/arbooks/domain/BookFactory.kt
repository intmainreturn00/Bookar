package com.intmainreturn00.arbooks.domain

import android.content.Context
import com.intmainreturn00.arbooks.R
import com.intmainreturn00.arbooks.downloadImage
import com.intmainreturn00.arbooks.ui.dpToPix
import com.intmainreturn00.grapi.Review
import org.michaelevans.colorart.library.ColorArt
import kotlin.random.Random


fun makeOpenlibLink(isbn: String) = "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg"

interface BookFactory {
    companion object {

        suspend fun createFromReview(review: Review, ctx: Context): Book {
            val url = when {
                !review.book.imageUrl.contains("nophoto") -> review.book.imageUrl
                review.book.isbn.isNotEmpty() -> makeOpenlibLink(review.book.isbn)
                else -> ""
            }

            downloadImage(ctx, url).let {
                if (it != null) {
                    val spineColor = ColorArt(it).backgroundColor
                    val c = ImageCover(it.width, it.height, spineColor, url)
                    return makeBookWithCover(review, c)
                } else {
                    val width = dpToPix(ctx, 75f).toInt()
                    val height = dpToPix(ctx, 120f).toInt()
                    val (spineColor, textColor) = makeRandomTemplateColors(ctx)
                    val author = if (review.book.authors.isNotEmpty()) {
                        review.book.authors[0].name
                    } else {
                        ""
                    }
                    val c = TemplateCover(width, height, spineColor, textColor, review.book.titleWithoutSeries, author)
                    return makeBookWithCover(review, c)
                }
            }
        }

        private fun makeBookWithCover(review: Review, c: Cover): Book = Book(
            id = review.book.id.toInt(),
            pages = review.book.numPages,
            rating = review.rating,
            readCount = review.readCount,
            cover = c
        )

        private fun makeRandomTemplateColors(ctx: Context) = when (Random.nextInt(3)) {
            0 -> Pair(ctx.getColor(R.color.red), ctx.getColor(R.color.orange))
            1 -> Pair(ctx.getColor(R.color.orange), ctx.getColor(R.color.dark))
            else -> Pair(ctx.getColor(R.color.dark), ctx.getColor(R.color.orange))
        }
    }
}