package com.example.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.activityViewModels
import com.nice.common.app.NiceViewModelFragment
import com.nice.common.helper.add
import com.nice.common.helper.doOnClick
import com.nice.common.helper.showIme
import com.nice.common.viewmodel.Message
import com.nice.common.widget.TipView
import com.nice.common.widget.defaultSnackTipViewFactory
import com.nice.common.widget.tipViews

class FirstFragment : NiceViewModelFragment<TestViewModel>(R.layout.fragment_first) {

    override val viewModel: TestViewModel by activityViewModels()

    override val tipView: TipView? by tipViews{ defaultSnackTipViewFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.button).doOnClick {
            findViewById<View>(R.id.edittext).showIme()
            viewModel.message = Message.Tip("hello world")
        }

        childFragmentManager.add<ChildFragment>(R.id.frame_container)
    }

    override fun dispatchViewModelMessage(message: Message): Boolean {
        Log.e(TAG, "dispatchViewModelMessage: " + message.javaClass.simpleName)
        return super.dispatchViewModelMessage(message)
    }

    override fun onInterceptViewModelMessage(message: Message): Boolean {
        Log.e(TAG, "onInterceptViewModelMessage: " + message.javaClass.simpleName)
        return super.onInterceptViewModelMessage(message)
    }

    override fun onViewModelMessage(message: Message): Boolean {
        Log.e(TAG, "onViewModelMessage: " + message.javaClass.simpleName)
        return super.onViewModelMessage(message)
    }

    companion object {
        private val TAG = FirstFragment::class.simpleName
    }

}