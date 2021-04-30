package com.example.sample

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.example.sample.databinding.FragmentSecondBinding
import com.nice.kotlins.adapter.ItemViewHolder
import com.nice.kotlins.adapter.plusAssign
import com.nice.kotlins.app.NiceFragment
import com.nice.kotlins.helper.ViewBindingFactory
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
        val start = System.currentTimeMillis()
        binding.attachTo(this)

        Log.e("TAGTAG", "time: ${System.currentTimeMillis() - start}")

        binding.recyclerView.addItemDecoration(LinearDividerItemDecoration())

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