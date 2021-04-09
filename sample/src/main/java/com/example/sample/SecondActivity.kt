package com.example.sample

import android.os.Bundle
import com.easy.kotlins.app.NiceActivity
import com.easy.kotlins.helper.add

class SecondActivity: NiceActivity(R.layout.activity_second) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.add<MainFragment>(R.id.frame_container)

    }

}