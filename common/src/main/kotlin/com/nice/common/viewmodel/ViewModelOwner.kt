package com.nice.common.viewmodel

import androidx.lifecycle.ViewModel

interface ViewModelOwner<VM : ViewModel> {

    val viewModel: VM

}