package com.intmainreturn00.arbooks.ui

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intmainreturn00.arbooks.R
import com.intmainreturn00.arbooks.domain.Book
import com.intmainreturn00.arbooks.domain.Cover
import com.intmainreturn00.arbooks.domain.ImageCover
import com.intmainreturn00.arbooks.domain.TemplateCover
import com.intmainreturn00.arbooks.platform.GlideApp
import kotlinx.android.synthetic.main.book.view.*


fun ImageView.gray(on: Boolean) {
    if (on) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)  //0 means grayscale
        val cf = ColorMatrixColorFilter(matrix)
        colorFilter = cf
        imageAlpha = 153   // 128 = 0.5, 153 = 0.6
    } else {
        colorFilter = null
        imageAlpha = 255
    }
}


class ShelfAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val books = mutableListOf<Book>()

    var selected = true
        set(value) {
            field = value
            notifyItemRangeChanged(0, books.size)
        }

    fun addBook(book: Book) {
        books.add(book)
        notifyItemInserted(books.size - 1)
    }

    fun setBooks(books: MutableList<Book>) {
        this.books.clear()
        this.books.addAll(books)
        notifyItemInserted(books.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        Cover.TypeImage -> CoverViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.book, parent, false)
        )
        else -> TemplateViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.book_template_shadow, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val book = books[position]
        val notUsed = when (book.cover) {
            is ImageCover -> {
                val coverHolder = holder as CoverViewHolder
                GlideApp.with(context).load(book.cover.url).into(coverHolder.img)
                holder.img.gray(!selected)
            }
            is TemplateCover -> {
                val templateHolder = holder as TemplateViewHolder
                templateHolder.title.text = book.cover.title
                templateHolder.title.setCustomFont(PodkovaFont.EXTRA_BOLD)
                templateHolder.author.setCustomFont(PodkovaFont.REGULAR)

                templateHolder.background.setBackgroundColor(book.cover.spineColor)
                templateHolder.title.setTextColor(book.cover.textColor)
                templateHolder.author.setTextColor(book.cover.textColor)
                templateHolder.author.text = book.cover.author
                templateHolder.gray(!selected, book.cover)
            }
        }
    }

    override fun getItemCount(): Int = books.size

    override fun getItemId(position: Int): Long = books[position].id.toLong()

    override fun getItemViewType(position: Int): Int = Cover.toInt(books[position].cover)

    inner class CoverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.img
    }

    inner class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val author: TextView = itemView.findViewById(R.id.author)
        val background: View = itemView.findViewById(R.id.background)

        fun gray(on: Boolean, cover: TemplateCover) {
            if (on) {
                background.setBackgroundColor(context.resources.getColor(R.color.grey_background))
                title.setTextColor(context.resources.getColor(R.color.grey_font))
                author.setTextColor(context.resources.getColor(R.color.grey_font))
            } else {
                background.setBackgroundColor(cover.spineColor)
                title.setTextColor(cover.textColor)
                author.setTextColor(cover.textColor)
            }
        }
    }
}