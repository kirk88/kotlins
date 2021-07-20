package com.example.sample

import androidx.lifecycle.viewModelScope
import com.nice.common.event.errorShow
import com.nice.common.event.loadingShow
import com.nice.common.viewmodel.NiceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : NiceViewModel() {

    fun start() {
        event = loadingShow()


        viewModelScope.launch {
            delay(500)

            event = errorShow()
        }
    }

}