package com.example.sample

import androidx.lifecycle.viewModelScope
import com.nice.kotlins.event.errorShow
import com.nice.kotlins.event.loadingShow
import com.nice.kotlins.viewmodel.NiceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel:NiceViewModel() {

    fun start(){

        event = loadingShow()


        viewModelScope.launch {
            delay(500)


            event = errorShow()
        }

    }

}