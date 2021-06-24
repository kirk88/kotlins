package com.example.sample

import android.graphics.Color
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
import com.nice.kotlins.widget.LoadState
import com.nice.kotlins.widget.LoadableRecyclerView
import com.nice.kotlins.widget.adapter
import com.nice.kotlins.widget.divider.GridDividerItemDecoration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            .register(ViewHolderCreator { inflater, parent ->
                ItemViewHolder(AppCompatTextView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(-1, 200)
                })
            }).bind(ViewHolderBinder { holder, item, payloads ->
                (holder.itemView as TextView).string = "TEXT"
            })
            .build()

        binding.recyclerView.addItemDecoration(GridDividerItemDecoration(Color.BLUE, 8))

        binding.recyclerView.adapter = adapter

        binding.recyclerView.setOnRefreshLoadMoreListener(object :
            LoadableRecyclerView.OnRefreshLoadMoreListener {

            var page = 0

            override fun onRefresh() {
                Log.e("TAGTAG", "onRefresh")
                adapter.setItems(mutableListOf<String>().apply {
                    repeat(20) {
                        add("")
                    }
                })

                lifecycleScope.launch {
                    delay(400)

                    binding.recyclerView.setRefreshState(LoadState.STATE_COMPLETED)
                }
            }

            override fun onLoadMore() {
                Log.e("TAGTAG", "onLoadMore")

                lifecycleScope.launch {
                    delay(400)

                    adapter.addItems(mutableListOf<String>().apply {
                        repeat(20) {
                            add("")
                        }
                    })

                    binding.recyclerView.setLoadMoreState(LoadState.STATE_IDLE)
                }
            }

        })

    }

}