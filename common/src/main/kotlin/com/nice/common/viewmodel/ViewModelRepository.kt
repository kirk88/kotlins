package com.nice.common.viewmodel

import java.io.Closeable

abstract class ViewModelRepository<SOURCE : ViewModelDataSource>(val dataSource: SOURCE): Closeable {

    override fun close() {
        dataSource.close()
    }

}