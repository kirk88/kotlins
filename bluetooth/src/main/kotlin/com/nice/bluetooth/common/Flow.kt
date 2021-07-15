package com.nice.bluetooth.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal fun <T> Flow<T>.launchIn(
    scope: CoroutineScope,
    start: CoroutineStart
): Job = scope.launch(start = start) {
    collect()
}
