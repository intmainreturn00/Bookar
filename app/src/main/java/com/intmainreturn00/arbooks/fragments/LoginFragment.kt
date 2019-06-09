package com.intmainreturn00.arbooks.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.intmainreturn00.arbooks.R
import com.intmainreturn00.arbooks.ScopedFragment
import com.intmainreturn00.grapi.grapi
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.browse


class LoginFragment : ScopedFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-Medium.ttf")
        intro.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-Regular.ttf")
        bookar.typeface = Typeface.createFromAsset(context?.assets, "fonts/Podkova-ExtraBold.ttf")

        login.setOnClickListener {
            launch {
                grapi.loginStart()
                activity?.browse(grapi.getAuthorizationUrl())
            }
        }
    }
}
