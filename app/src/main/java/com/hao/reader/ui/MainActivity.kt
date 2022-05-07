package com.hao.reader.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.hao.reader.extension.setDecorFitsSystemWindows


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDecorFitsSystemWindows(false)

    }


}