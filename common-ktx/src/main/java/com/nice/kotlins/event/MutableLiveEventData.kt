@file:Suppress("unused")

package com.nice.kotlins.event

open class MutableLiveEventData<T> : LiveEventData<T> {

    constructor() : super()
    constructor(value: T) : super(value)

    public override fun postValue(value: T) {
        super.postValue(value)
    }

    public override fun setValue(value: T?) {
        super.setValue(value)
    }

}