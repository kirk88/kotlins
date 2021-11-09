package com.example.sample

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.example.sample.databinding.FragmentThirdBinding
import com.nice.common.adapter.ItemViewHolder
import com.nice.common.app.NiceFragment
import com.nice.common.helper.adapterBuilder
import com.nice.common.helper.setContentView
import com.nice.common.helper.string
import com.nice.common.helper.viewBindings

class ThirdFragment : NiceFragment() {

    private val binding: FragmentThirdBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        val adapter = adapterBuilder<String, ItemViewHolder>(requireContext(),
            mutableListOf<String>().apply {
                repeat(20) {
                    add("")
                }
            })
            .register { inflater, parent ->
                ItemViewHolder(AppCompatTextView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(-1, 200)
                })
            }.bind { holder, item, payloads ->
                (holder.itemView as TextView).string = "TEXT${holder.layoutPosition}"
            }
            .build()

    }


}