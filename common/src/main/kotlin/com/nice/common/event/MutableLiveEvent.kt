@file:Suppress("unused")

package com.nice.common.event

open class MutableLiveEvent<T> : LiveEvent<T> {

    constructor() : super()
    constructor(value: T) : super(value)

    public override fun postValue(value: T) {
        super.postValue(value)
    }

    public override fun setValue(value: T?) {
        super.setValue(value)
    }

}

var <T> MutableLiveEvent<T>.value: T?
    get() = getValue()
    set(value) {
        setValue(value)
    }
