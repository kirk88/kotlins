package com.nice.common.viewmodel

import java.io.Closeable


abstract class ViewModelDataSource : Closeable {

    override fun close() {
    }

}