package com.intmainreturn00.arbooks.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


import com.intmainreturn00.arbooks.R
import com.intmainreturn00.grapi.grapi

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (grapi.isLoggedIn() || activity?.intent?.data != null ) {
            findNavController().navigate(R.id.action_main_to_loading)
        } else {
            findNavController().navigate(R.id.action_main_to_login)
        }
    }

}
