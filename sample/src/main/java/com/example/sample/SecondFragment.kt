package com.example.sample

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.FragmentSecondBinding
import com.nice.kotlins.adapter.ItemViewHolder
import com.nice.kotlins.adapter.anim.ScaleInAnimation
import com.nice.kotlins.adapter.plusAssign
import com.nice.kotlins.app.NiceFragment
import com.nice.kotlins.helper.adapterBuilder
import com.nice.kotlins.helper.setContentView
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.divider.LinearDividerItemDecoration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
        setContentView(binding)

        Log.e("TAGTAG", "time: ${System.currentTimeMillis() - start}")

        binding.recyclerView.addItemDecoration(LinearDividerItemDecoration())

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

        lifecycleScope.launch {
            delay(2000)

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
        }

//        binding.listview.adapter = ArrayAdapter(requireContext(), R.layout.item_recycler_view, R.id.title, )
    }

}