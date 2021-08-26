package com.example.sample

import androidx.lifecycle.viewModelScope
import com.nice.common.event.Message
import com.nice.common.viewmodel.NiceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : NiceViewModel() {

    fun start() {
        message = Message.ShowLoading()


        viewModelScope.launch {
            delay(500)

            message = Message.ShowError()
        }
    }

}