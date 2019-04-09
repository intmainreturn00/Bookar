package com.intmainreturn00.arbooks

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = sceneform_fragment as ArFragment
    }
}
