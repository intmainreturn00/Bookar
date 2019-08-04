package com.intmainreturn00.arbooks.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.navigation.Navigation.findNavController
import com.intmainreturn00.arbooks.R
import com.intmainreturn00.arbooks.platform.ScopedAppActivity

class MainActivity : ScopedAppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )

        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

    }

    override fun onSupportNavigateUp() = findNavController(this, R.id.nav_host_fragment).navigateUp()


}
