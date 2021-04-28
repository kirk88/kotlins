package com.example.sample

import android.os.Bundle
import android.widget.TextView
import com.example.sample.databinding.FragmentSecondBinding
import com.nice.kotlins.adapter.ItemViewHolder
import com.nice.kotlins.adapter.plusAssign
import com.nice.kotlins.app.NiceFragment
import com.nice.kotlins.helper.adapterBuilder
import com.nice.kotlins.helper.attachTo
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.divider.LinearDividerItemDecoration


class SecondFragment : NiceFragment() {

    private val binding: FragmentSecondBinding by viewBindings()

    val adapter by lazy {
        adapterBuilder<String, ItemViewHolder>(requireContext())
            .register { inflater, parent ->
                ItemViewHolder(inflater.inflate(R.layout.item_recycler_view, parent, false))
            }.bind { holder, item, _ ->
                holder.findViewById<TextView>(R.id.title)?.text = item
            }.into(binding.recyclerView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.attachTo(this)

        binding.recyclerView.addItemDecoration(LinearDividerItemDecoration(requireContext()))

//        adapter += listOf(
//            "abcd",
//            "abcde",
//            "abcdef",
//            "abcd",
//            "abcd",
//            "abcde",
//            "abcdef",
//            "abcd",
//            "abcd",
//            "abcde",
//            "abcdef",
//            "abcd",
//            "abcd",
//            "abcde",
//            "abcdef",
//            "abcd",
//            "abcd",
//            "abcde",
//            "abcdef",
//            "abcd",
//            "abcd",
//            "abcde",
//            "abcdef",
//            "abcd",
//            "abcd",
//            "abcde",
//            "abcdef",
//            "abcd",
//            "abcd",
//            "abcde",
//            "abcdef",
//            "abcd"
//        )

//        binding.listview.adapter = ArrayAdapter(requireContext(), R.layout.item_recycler_view, R.id.title, )
    }

}