package com.example.sample

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.FragmentThirdBinding
import com.nice.kotlins.adapter.ItemViewHolder
import com.nice.kotlins.app.NiceFragment
import com.nice.kotlins.helper.*
import com.nice.kotlins.widget.RefreshRecyclerView
import com.nice.kotlins.widget.adapter
import com.nice.kotlins.widget.isLoadingMore
import com.nice.kotlins.widget.isRefreshing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ThirdFragment : NiceFragment() {


    private val binding: FragmentThirdBinding by viewBindings()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        val adapter = adapterBuilder<String, ItemViewHolder>(requireContext(),
            mutableListOf<String>().apply {
                repeat(10) {
                    add("")
                }
            })
            .register(ViewHolderCreator { inflater, parent ->
                ItemViewHolder(AppCompatTextView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(-1, 200)
                })
            }).bind(ViewHolderBinder { holder, item, payloads ->
                (holder.itemView as TextView).string = "TEXT"
            })
            .build()

        binding.recyclerView.adapter = adapter

        binding.recyclerView.setOnRefreshLoadMoreListener(object : RefreshRecyclerView.OnRefreshLoadMoreListener{

            var count = 0

            override fun onRefresh() {
               Log.e("TAGTAG", "onRefresh")
                adapter.setItems(mutableListOf<String>().apply {
                    repeat(10) {
                        add("")
                    }
                })

                lifecycleScope.launch {
                    delay(1000)

                    binding.recyclerView.isRefreshing = false
                }
            }

            override fun onLoadMore() {
                Log.e("TAGTAG", "onLoadMore")

                count += 1

                lifecycleScope.launch {
                    delay(1000)

                    if(count >= 3){
                     binding.recyclerView.setLoadMoreState(RefreshRecyclerView.STATE_COMPLETED)
                    }else {
                        adapter.addItems(mutableListOf<String>().apply {
                            repeat(10) {
                                add("")
                            }
                        })


                        binding.recyclerView.isLoadingMore = false
                    }
                }
            }

        })

    }

}