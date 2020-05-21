package com.elliot.kim.kotlin.dimcatcamnote

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    var title: String = "",
    var creationTime: Long,
    var uri: String?) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
    var content: String = ""
    var editTime: Long? = null
    var alarmTime: Long? = null
    var isDone: Boolean = false
    var folderId: Int? = null

    fun toSharedString(): String =
        """
        $title
        최초 작성일: $creationTime
        ${if (editTime == null) "" else "최근 수정일: $editTime"}
        내용:
        $content
        """.trimIndent()
}