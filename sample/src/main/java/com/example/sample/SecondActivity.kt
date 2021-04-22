package com.example.sample

import android.os.Bundle
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.nice.kotlins.app.NiceActivity

class SecondActivity : NiceActivity(R.layout.activity_second) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.commit {
            add<SecondFragment>(R.id.frame_container)
        }

    }


}