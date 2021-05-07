package com.example.sample

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.android.material.textview.MaterialTextView
import com.nice.kotlins.app.NiceFragment
import com.nice.kotlins.helper.onClick
import com.nice.kotlins.helper.showIme
import com.nice.kotlins.helper.sp

class FirstFragment: NiceFragment(R.layout.fragment_first) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val editText = findViewById<EditText>(R.id.edittext)
        findViewById<Button>(R.id.button).onClick {
            editText.showIme()
        }

    }


}