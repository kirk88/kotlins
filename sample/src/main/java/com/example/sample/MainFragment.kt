package com.example.sample

import android.os.Bundle
import android.view.*
import com.example.sample.databinding.FragmentMainBinding
import com.example.sample.databinding.ItemRecyclerViewBinding
import com.nice.kotlins.adapter.ViewBindingRecyclerAdapter
import com.nice.kotlins.adapter.plusAssign
import com.nice.kotlins.app.NiceFragment
import com.nice.kotlins.app.subtitle
import com.nice.kotlins.app.title
import com.nice.kotlins.helper.attachTo
import com.nice.kotlins.helper.toast
import com.nice.kotlins.helper.viewBindings

class MainFragment : NiceFragment() {

    init {
        setHasOptionsMenu(true)
    }

    private val binding: FragmentMainBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.attachTo(this)

        title = "Hello World"
        subtitle = "Bye World"

        binding.textView.text = "Hello World"

        val adapter =
            object : ViewBindingRecyclerAdapter<String, ItemRecyclerViewBinding>(requireContext()) {

                override fun onCreateItemView(
                    inflater: LayoutInflater,
                    parent: ViewGroup,
                    viewType: Int
                ): ItemRecyclerViewBinding {
                    return ItemRecyclerViewBinding.inflate(inflater, parent, false)
                }

                override fun onBindItemView(
                    binding: ItemRecyclerViewBinding,
                    item: String,
                    payloads: MutableList<Any>
                ) {
                    binding.title.text = item
                }


            }

        binding.recyclerView.adapter = adapter

        adapter += listOf("abcd", "abcde", "abcdef")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add("测试").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
        } else if (item.title == "测试") {
            toast("哈哈哈哈哈")
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}