package com.intmainreturn00.arbooks

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.book.view.img
import kotlinx.android.synthetic.main.shelf.view.*


data class ShelfModel(
    val title: String = "",
    val images: MutableList<BookModel>
)


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


class ShelfAdapter(val context: Context, private val books: MutableList<BookModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var gray = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        TYPE_COVER -> CoverViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.book, parent, false))
        TYPE_TEMPLATE1 -> TemplateViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.book_template1, parent, false), TYPE_TEMPLATE1
        )
        TYPE_TEMPLATE2 -> TemplateViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.book_template2, parent, false), TYPE_TEMPLATE2
        )
        else -> TemplateViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.book_template3, parent, false), TYPE_TEMPLATE3
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val book = books[position]
        when (getItemViewType(position)) {
            TYPE_COVER -> {
                val coverHolder = holder as CoverViewHolder
                GlideApp.with(context).load(book.cover).into(coverHolder.img)
                holder.img.gray(gray)
            }
            else -> {
                val templateHolder = holder as TemplateViewHolder
                templateHolder.title.text = book.title
                templateHolder.title.setCustomFont(PodkovaFont.EXTRA_BOLD)
                templateHolder.author1.setCustomFont(PodkovaFont.REGULAR)
                templateHolder.author2.setCustomFont(PodkovaFont.REGULAR)
                templateHolder.gray(gray)
                when (book.authors.size) {
                    0 -> {
                        templateHolder.author1.text = ""
                        templateHolder.author2.text = ""
                        templateHolder.author1.visibility = GONE
                        templateHolder.author2.visibility = GONE
                    }
                    1 -> {
                        templateHolder.author1.text = book.authors[0]
                        templateHolder.author2.text = ""
                        templateHolder.author1.visibility = VISIBLE
                        templateHolder.author2.visibility = GONE
                    }
                    else -> {
                        templateHolder.author1.text = book.authors[0]
                        templateHolder.author2.text = book.authors[1]
                        templateHolder.author1.visibility = VISIBLE
                        templateHolder.author2.visibility = VISIBLE
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = books.size

    override fun getItemViewType(position: Int): Int {
        val book = books[position]
        return when {
            book.cover.isNotEmpty() -> TYPE_COVER
            position % 3 == 0 -> TYPE_TEMPLATE1
            position % 3 == 1 -> TYPE_TEMPLATE2
            else -> TYPE_TEMPLATE3
        }
    }

    inner class CoverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.img
    }

    inner class TemplateViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        val type = viewType
        val title: TextView = itemView.findViewById(R.id.title)
        val author1: TextView = itemView.findViewById(R.id.author1)
        val author2: TextView = itemView.findViewById(R.id.author2)
        val background: View = itemView.findViewById(R.id.background)

        fun gray(on: Boolean) {
            if (on) {
                background.setBackgroundColor(context.resources.getColor(R.color.grey_background))
                title.setTextColor(context.resources.getColor(R.color.grey_font))
                author1.setTextColor(context.resources.getColor(R.color.grey_font))
                author2.setTextColor(context.resources.getColor(R.color.grey_font))
            } else {
                when (type) {
                    TYPE_TEMPLATE1 -> {
                        background.setBackgroundColor(context.resources.getColor(R.color.red))
                        title.setTextColor(context.resources.getColor(R.color.orange))
                        author1.setTextColor(context.resources.getColor(R.color.orange))
                        author2.setTextColor(context.resources.getColor(R.color.orange))
                    }
                    TYPE_TEMPLATE2 -> {
                        background.setBackgroundColor(context.resources.getColor(R.color.orange))
                        title.setTextColor(context.resources.getColor(R.color.dark))
                        author1.setTextColor(context.resources.getColor(R.color.dark))
                        author2.setTextColor(context.resources.getColor(R.color.dark))
                    }
                    else -> {
                        background.setBackgroundColor(context.resources.getColor(R.color.dark))
                        title.setTextColor(context.resources.getColor(R.color.orange))
                        author1.setTextColor(context.resources.getColor(R.color.orange))
                        author2.setTextColor(context.resources.getColor(R.color.orange))
                    }
                }
            }
        }
    }

    companion object {
        private const val TYPE_COVER = 0
        private const val TYPE_TEMPLATE1 = 1
        private const val TYPE_TEMPLATE2 = 2
        private const val TYPE_TEMPLATE3 = 3
    }
}


class ShelvesAdapter(
    val context: Context,
    private val shelves: MutableList<ShelfModel>,
    val onShelf: (on: Boolean, position: Int) -> Unit
) : RecyclerView.Adapter<ShelvesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.shelf, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = shelves.size

    fun notifyBookAdded(shelf: Int) {
        shelfAdapters.getOrNull(shelf)?.notifyDataSetChanged()
    }

    private val shelfAdapters = MutableList(shelves.size) {
        ShelfAdapter(context, mutableListOf())
    }

    var allowEditing = false

    fun addShelf() {
        shelfAdapters.add(ShelfAdapter(context, mutableListOf()))
//        shelfAdapters.add(0, ShelfAdapter(context, mutableListOf()))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shelf = shelves[position]
        holder.shelfTitle.text = shelf.title
        holder.shelfTitle.setCustomFont(PodkovaFont.EXTRA_BOLD)
        val childLayoutManager = LinearLayoutManager(holder.shelf.context, LinearLayout.HORIZONTAL, false)
        childLayoutManager.initialPrefetchItemCount = 4

        shelfAdapters[position] = ShelfAdapter(context, shelf.images)

        if (allowEditing) {
            holder.header.on.visibility = VISIBLE
        }

        holder.root.setOnClickListener {
            if (holder.on.tag == "on") {
                holder.on.tag = "off"
                holder.on.setImageResource(R.drawable.off)
                holder.shelfTitle.alpha = 0.4f
                shelfAdapters.getOrNull(position)?.gray = true
                shelfAdapters.getOrNull(position)?.notifyDataSetChanged()
                onShelf(false, position)
            } else {
                holder.on.tag = "on"
                holder.on.setImageResource(R.drawable.on)
                holder.shelfTitle.alpha = 1f
                shelfAdapters.getOrNull(position)?.gray = false
                shelfAdapters.getOrNull(position)?.notifyDataSetChanged()
                onShelf(true, position)
            }
        }


        holder.shelf.apply {
            layoutManager = childLayoutManager
            adapter = shelfAdapters[position]
        }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shelf: RecyclerView = itemView.shelf
        val shelfTitle: TextView = itemView.shelf_title
        val on: ImageView = itemView.on
        val header: LinearLayout = itemView.header
        val root: LinearLayout = itemView.root
    }
}