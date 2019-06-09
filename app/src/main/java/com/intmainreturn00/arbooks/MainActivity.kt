package com.intmainreturn00.arbooks

import android.os.Bundle
import android.view.WindowManager
import androidx.navigation.Navigation.findNavController

class MainActivity : ScopedAppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

//        if (grapi.isLoggedIn()) {
//            Navigation.findNavController(this, R.id.nav_host_fragment)
//                .navigate(R.id.action_mainFragment_to_loadingFragment)
//        } else {
//            Navigation.findNavController(this, R.id.nav_host_fragment)
//                .navigate(R.id.action_mainFragment_to_loginFragment)
//        }

        /*


        if (grapi.isLoggedIn()) {
            // already logged in
            loadData()
        } else {
            if (intent.data == null) {
                // regular launch - not logged in
                login.visibility = VISIBLE
            } else {
                // auth redirect
                login.visibility = INVISIBLE

                launch {
                    grapi.loginEnd(intent) { ok ->
                        if (ok) {
                            loadData()
                        } else {
                            Toasty.error(
                                this@MainActivity,
                                resources.getString(R.string.login_error),
                                Toast.LENGTH_SHORT, true
                            ).show()
                        }
                    }
                }
            }

            login.setOnClickListener {
                launch {
                    grapi.loginStart()
                    browse(grapi.getAuthorizationUrl())
                }
            }
        }*/

    }

    override fun onSupportNavigateUp() = findNavController(this, R.id.nav_host_fragment).navigateUp()


}
