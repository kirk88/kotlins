package com.easy.kotlins.viewmodel

import androidx.lifecycle.ViewModel

interface ViewModelOwner<VM : ViewModel> {

    val viewModel: VM

}