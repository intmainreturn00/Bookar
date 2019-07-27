package com.intmainreturn00.arbooks.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intmainreturn00.arbooks.ui.PodkovaFont

import com.intmainreturn00.arbooks.R
import com.intmainreturn00.arbooks.platform.ScopedFragment
import com.intmainreturn00.arbooks.ui.setCustomFont
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

        login.setCustomFont(PodkovaFont.MEDIUM)
        intro.setCustomFont(PodkovaFont.REGULAR)
        bookar.setCustomFont(PodkovaFont.EXTRA_BOLD)

        login.setOnClickListener {
            launch {
                grapi.loginStart()
                activity?.browse(grapi.getAuthorizationUrl())
            }
        }
    }
}
