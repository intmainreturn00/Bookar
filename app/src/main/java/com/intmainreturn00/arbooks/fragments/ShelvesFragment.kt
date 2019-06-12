package com.intmainreturn00.arbooks.fragments


import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.request.RequestOptions
import com.intmainreturn00.arbooks.BooksViewModel
import com.intmainreturn00.arbooks.GlideApp

import com.intmainreturn00.arbooks.R
import com.intmainreturn00.arbooks.formatProfileAge
import kotlinx.android.synthetic.main.fragment_shelves.*

class ShelvesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shelves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.run {
            val model = ViewModelProviders.of(this).get(BooksViewModel::class.java)
            GlideApp.with(this).load(model.user.imageUrl).apply(RequestOptions().circleCrop()).into(avatar)

            books_num.text = model.numBooks.toString()
            books_num.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-ExtraBold.ttf")
            books.text = resources.getQuantityString(R.plurals.books, model.numBooks)
            books.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-ExtraBold.ttf")

            pages_num.text = model.numPages.toString()
            pages_num.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-ExtraBold.ttf")
            pages.text = resources.getQuantityString(R.plurals.pages, model.numPages)
            pages.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-ExtraBold.ttf")

            val (num, qualifier) = formatProfileAge(model.user.joined)
            age_num.text = num
            age_num.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-ExtraBold.ttf")
            age.text = qualifier
            age.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-ExtraBold.ttf")

            status.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-Regular.ttf")
        }


    }


}
