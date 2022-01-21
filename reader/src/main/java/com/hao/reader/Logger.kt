package com.hao.reader

import com.nice.kothttp.BuildConfig
import java.util.logging.Level

object Logger {

    private val logger = java.util.logging.Logger.getLogger("Monitor")

    fun debug(message: Any) {
        if (BuildConfig.DEBUG) {
            logger.log(Level.INFO, message.toString())
        }
    }

    fun error(message: Any, error: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            logger.log(Level.WARNING, message.toString(), error)
        }
    }

}