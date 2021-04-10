package com.nice.kotlins.viewmodel

abstract class ViewModelRepository<SOURCE: ViewModelDataSource>(private val dataSource: SOURCE) {

    open fun clear(){
        dataSource.clear()
    }

}