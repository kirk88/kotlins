package com.easy.kotlins.viewmodel

abstract class ViewModelRepository<SOURCE: ViewModelDataSource>(private val dataSource: SOURCE) {

    open fun clear(){
        dataSource.clear()
    }

}