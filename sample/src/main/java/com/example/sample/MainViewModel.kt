package com.example.sample

import androidx.lifecycle.viewModelScope
import com.nice.common.viewmodel.NiceViewModel
import com.nice.common.viewmodel.ShowContent
import com.nice.common.viewmodel.ShowError
import com.nice.common.viewmodel.ShowLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : NiceViewModel() {

    fun start() {
        message = ShowLoading()

        viewModelScope.launch {
            delay(3000)

            message = ShowError()

            delay(3000)

            message = ShowContent()
        }

    }


}