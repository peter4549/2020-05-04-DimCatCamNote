package com.elliot.kim.kotlin.dimcatcamnote.data

class Folder(val id: Int, val name: String) {
    var isLocked: Boolean = false
    var password: String = ""
    var noteIdSet: MutableSet<Int> = mutableSetOf()
}