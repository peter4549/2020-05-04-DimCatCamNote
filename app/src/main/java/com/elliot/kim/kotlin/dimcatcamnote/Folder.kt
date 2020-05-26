package com.elliot.kim.kotlin.dimcatcamnote

class Folder(val id: Int, val name: String) {

    private val noteIds: MutableSet<String>? = mutableSetOf()
    var isLocked: Boolean = false
    var password: String? = null
}