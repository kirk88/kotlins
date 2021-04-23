package com.example.sample

import android.R.attr.left
import android.R.attr.right
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sample.databinding.FragmentSecondBinding
import com.example.sample.databinding.ItemRecyclerViewBinding
import com.nice.kotlins.adapter.ViewBindingHolder
import com.nice.kotlins.adapter.ViewBindingRecyclerAdapter
import com.nice.kotlins.adapter.plusAssign
import com.nice.kotlins.app.NiceFragment
import com.nice.kotlins.app.subtitle
import com.nice.kotlins.app.title
import com.nice.kotlins.helper.attachTo
import com.nice.kotlins.helper.toast
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.divider.Divider
import com.nice.kotlins.widget.divider.DividerBuilder
import com.nice.kotlins.widget.divider.XDividerItemDecoration


class SecondFragment : NiceFragment() {

    init {
        setHasOptionsMenu(true)
    }

    private val binding: FragmentSecondBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.attachTo(this)

        title = "Hello World"
        subtitle = "Bye World"

        val adapter =
            object : ViewBindingRecyclerAdapter<String, ItemRecyclerViewBinding>(requireContext()) {

                override fun onCreateItemView(
                    inflater: LayoutInflater,
                    parent: ViewGroup,
                    viewType: Int
                ): ItemRecyclerViewBinding {
                    return ItemRecyclerViewBinding.inflate(inflater, parent, false)
                }


                override fun onBindItemViewHolder(
                    holder: ViewBindingHolder<ItemRecyclerViewBinding>,
                    item: String,
                    payloads: MutableList<Any>
                ) {
                    holder.binding.title.text = item
                }

            }

        binding.recyclerView.adapter = adapter

        adapter += listOf("abcd", "abcde", "abcdef","abcd", "abcde", "abcdef","abcd", "abcde", "abcdef","abcd", "abcde", "abcdef","abcd", "abcde", "abcdef","abcd", "abcde", "abcdef")

        binding.recyclerView.addItemDecoration(object: XDividerItemDecoration(){
            override fun getDivider(parent: RecyclerView, child: View, position: Int): Divider {
                val layout = parent.layoutManager as GridLayoutManager
                val spanCount = layout.spanCount
                val eachWidth: Int = (spanCount - 1) * 100 / spanCount
                val left = position % spanCount * (100 - eachWidth)
                val right = eachWidth - left
                return DividerBuilder(requireContext())
                    .left(left, visible = false)
                    .right(100, offset = right, visible = position % spanCount != spanCount - 1)
                    .bottom(100)
                    .build()
            }
        })

        binding.listView.adapter = ArrayAdapter(requireContext(), R.layout.item_recycler_view, R.id.title, listOf("abcd", "abcde", "abcdef"))

        Log.e("TAGTAG", "onCreate")
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

}