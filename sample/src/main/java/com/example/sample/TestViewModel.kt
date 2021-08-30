package com.example.sample

import android.app.Activity
import com.nice.common.event.Message
import com.nice.common.helper.intentOf
import com.nice.common.viewmodel.NiceViewModel

class TestViewModel : NiceViewModel() {


    init {
        message = Message.Batch(setOf(
            Message.Tip("ok"),
            Message.SetActivityResult(Activity.RESULT_OK, intentOf("id" to 0.toLong()))
        ))
    }

}