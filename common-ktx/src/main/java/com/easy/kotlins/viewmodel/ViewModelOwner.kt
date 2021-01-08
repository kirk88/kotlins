package com.easy.kotlins.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Create by LiZhanPing on 2020/8/29
 */
interface ViewModelOwner<VM : ViewModel>{

    val viewModel: VM

}