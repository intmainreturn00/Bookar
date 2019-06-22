package com.intmainreturn00.arbooks

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.book.view.*
import kotlinx.android.synthetic.main.shelf.view.*
import android.graphics.ColorMatrixColorFilter
import android.graphics.ColorMatrix
import android.view.View.VISIBLE


data class ShelfModel(
    val title: String = "",
    val images: MutableList<String>
)

class ShelfAdapter(val context: Context, private val images: MutableList<String>) :
    RecyclerView.Adapter<ShelfAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.book, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = images.size

    var gray = false

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        GlideApp.with(context).load(images[position]).into(holder.img)
        if (gray) {
            setLocked(holder.img)
        } else {
            setUnlocked(holder.img)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.img
    }

    private fun setLocked(v: ImageView) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)  //0 means grayscale
        val cf = ColorMatrixColorFilter(matrix)
        v.colorFilter = cf
        v.imageAlpha = 128   // 128 = 0.5
    }

    private fun setUnlocked(v: ImageView) {
        v.colorFilter = null
        v.imageAlpha = 255
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