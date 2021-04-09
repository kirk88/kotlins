package com.example.sample

import android.os.Bundle
import android.util.Log
import android.view.*
import com.easy.kotlins.adapter.ViewBindingRecyclerAdapter
import com.easy.kotlins.adapter.plusAssign
import com.easy.kotlins.app.NiceFragment
import com.easy.kotlins.app.subtitle
import com.easy.kotlins.app.title
import com.easy.kotlins.helper.installTo
import com.easy.kotlins.helper.toast
import com.easy.kotlins.helper.viewBindings
import com.example.sample.databinding.FragmentMainBinding
import com.example.sample.databinding.ItemRecyclerViewBinding

class MainFragment : NiceFragment() {

    init {
        setHasOptionsMenu(true)
    }

    private val binding: FragmentMainBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.installTo(this)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("TAGTAG", "onViewCreated")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.e("TAGTAG", "onActivityCreated")
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.e("TAGTAG", "onPostCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.e("TAGTAG", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAGTAG", "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("TAGTAG", "onDestroyView")
    }


}