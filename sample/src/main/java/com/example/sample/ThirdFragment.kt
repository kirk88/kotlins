package com.example.sample

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.FragmentThirdBinding
import com.nice.kotlins.adapter.ItemViewHolder
import com.nice.kotlins.app.NiceFragment
import com.nice.kotlins.helper.*
import com.nice.kotlins.widget.InfiniteState
import com.nice.kotlins.widget.adapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ThirdFragment : NiceFragment() {


    private val binding: FragmentThirdBinding by viewBindings()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        val heights = mutableMapOf<Int, Int>()

        val adapter = adapterBuilder<String, ItemViewHolder>(requireContext(),
            mutableListOf<String>().apply {
                repeat(20) {
                    add("")
                }
            })
            .register(ViewHolderCreator { inflater, parent ->
                ItemViewHolder(AppCompatTextView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(-1, -2)
                })
            }).bind(ViewHolderBinder { holder, item, payloads ->
                (holder.itemView as TextView).string = "TEXT${holder.layoutPosition}"

                holder.itemView.layoutHeight = heights.getOrPut(holder.layoutPosition){
                    200.rangeTo(300).random()
                }
            })
            .build()

        binding.recyclerView.adapter = adapter

        var page = 0

        binding.recyclerView.doOnRefresh {
            page = 0

            adapter.setItems(mutableListOf<String>().apply {
                repeat(20) {
                    add("")
                }
            })

            lifecycleScope.launch {
                delay(400)

                binding.recyclerView.setRefreshState(InfiniteState.STATE_COMPLETED)
            }
        }

        binding.recyclerView.doOnLoadMore {
            lifecycleScope.launch {
                delay(400)

                if(page == 3){
                    binding.recyclerView.setLoadMoreState(InfiniteState.STATE_FAILED)
                    return@launch
                }

                adapter.addItems(mutableListOf<String>().apply {
                    repeat(20) {
                        add("")
                    }
                })

                binding.recyclerView.setLoadMoreState(InfiniteState.STATE_IDLE)

                page += 1
            }
        }
    }

}