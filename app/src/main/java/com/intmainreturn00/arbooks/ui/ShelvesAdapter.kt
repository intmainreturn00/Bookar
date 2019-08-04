package com.intmainreturn00.arbooks.ui

import android.content.Context
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intmainreturn00.arbooks.R
import com.intmainreturn00.arbooks.domain.Book
import com.intmainreturn00.arbooks.domain.Shelf
import kotlinx.android.synthetic.main.shelf.view.*


class ShelvesAdapter(
    private val context: Context,
    private val shelves: List<Shelf>,
    val onShelf: (shelfId: Int, on: Boolean) -> Unit
) : RecyclerView.Adapter<ShelvesAdapter.ViewHolder>() {

    private val shelfAdapters = SparseArray<ShelfAdapter>()

    init {
        shelves.forEach {
            val adapter = ShelfAdapter(context)
            adapter.setHasStableIds(true)
            shelfAdapters.put(it.id, adapter)
        }
    }


    fun addBook(book: Book) {
        book.shelves.forEach { shelf ->
            shelfAdapters.get(shelf.id)?.addBook(book)
        }
    }

    fun setBooks(books: List<Book>) {
        val booksByShelf = SparseArray<MutableList<Book>>()
        shelves.forEach { booksByShelf.put(it.id, mutableListOf()) }
        books.forEach { book ->
            book.shelves.forEach { shelf ->
                booksByShelf[shelf.id].add(book)
            }
        }
        booksByShelf.forEach { shelfId, books ->
            shelfAdapters.get(shelfId)?.setBooks(books)
        }
    }

    fun clearBooks() {
        shelves.forEach { shelfAdapters.get(it.id)?.clearBooks() }
    }

    var allowSelection = false
        set(value) {
            field = value
            notifyItemRangeChanged(0, shelves.size)
        }

    fun selectShelves(selectedShelves: SparseBooleanArray) {
        selectedShelves.forEach { shelfId, selected -> shelfAdapters.get(shelfId)?.selected = selected }
        notifyItemRangeChanged(0, shelves.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.shelf, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = shelves.size

    override fun getItemId(position: Int): Long = shelves[position].id.toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shelf = shelves[position]
        holder.shelfTitle.text = shelf.name
        holder.shelfTitle.setCustomFont(PodkovaFont.EXTRA_BOLD)

        if (allowSelection) {
            holder.header.on.visibility = View.VISIBLE
        } else {
            holder.header.on.visibility = View.GONE
        }

        if (shelfAdapters[shelf.id].selected) {
            holder.on.setImageResource(R.drawable.on)
        } else {
            holder.on.setImageResource(R.drawable.off)
        }

        holder.root.setOnClickListener {
            if (shelfAdapters[shelf.id].selected) {
                holder.on.setImageResource(R.drawable.off)
                holder.shelfTitle.alpha = 0.4f
                shelfAdapters[shelf.id].selected = false
                onShelf(shelf.id, false)
            } else {
                holder.on.setImageResource(R.drawable.on)
                holder.shelfTitle.alpha = 1f
                shelfAdapters[shelf.id].selected = true
                onShelf(shelf.id, true)
            }
        }

        holder.shelf.apply {
            layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
            adapter = shelfAdapters[shelf.id]
            itemAnimator = null
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