package com.nice.sqlite.core.ddl

interface Renderer {

    fun render(): String

    fun fullRender(): String = render()

}