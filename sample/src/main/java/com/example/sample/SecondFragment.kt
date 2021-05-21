package com.example.sample

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.example.sample.databinding.FragmentSecondBinding
import com.nice.kotlins.adapter.ItemViewHolder
import com.nice.kotlins.adapter.anim.ScaleInAnimation
import com.nice.kotlins.adapter.plusAssign
import com.nice.kotlins.app.NiceFragment
import com.nice.kotlins.helper.adapterBuilder
import com.nice.kotlins.helper.setContentView
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.divider.GridDividerItemDecoration
import com.nice.kotlins.widget.divider.LinearDividerItemDecoration


class SecondFragment : NiceFragment() {

    private val binding: FragmentSecondBinding by viewBindings()

    private val heights = mutableMapOf<Int, Int>()

    val adapter by lazy {
        adapterBuilder<String, ItemViewHolder>(requireContext())
            .register { inflater, parent ->
                ItemViewHolder(inflater.inflate(R.layout.item_recycler_view, parent, false))
            }.bind { holder, item, _ ->
                holder.itemView.updateLayoutParams<RecyclerView.LayoutParams> {
                    height = heights.getOrPut(holder.layoutPosition){
                        100.rangeTo(200).random()
                    }
                }
                holder.findViewById<TextView>(R.id.title)?.text = item
            }.into(binding.recyclerView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        binding.recyclerView.addItemDecoration(LinearDividerItemDecoration(Color.RED, 10))

        adapter.setItemAnimation(ScaleInAnimation(0.9f, 0.7f))

        adapter += listOf(
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd",
            "abcd",
            "abcde",
            "abcdef",
            "abcd"
        )

//        binding.listview.adapter = ArrayAdapter(requireContext(), R.layout.item_recycler_view, R.id.title, )
    }

}