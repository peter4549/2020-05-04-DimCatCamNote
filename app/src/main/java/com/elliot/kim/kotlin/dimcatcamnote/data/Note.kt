package com.elliot.kim.kotlin.dimcatcamnote.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elliot.kim.kotlin.dimcatcamnote.DEFAULT_FOLDER_ID

@Entity
data class Note(var title: String = "", var creationTime: Long, var uri: String?) {

    @PrimaryKey(autoGenerate = true) var id: Int = 0
    var content: String = ""
    var folderId: Int = DEFAULT_FOLDER_ID
    var editTime: Long? = null
    var alarmTime: Long? = null
    var isDeleted = false
    var isDone: Boolean = false
    var isLocked = false
    var password: String? = null
    var appWidgetIds: Array<Int> = arrayOf() // Dummy

    fun toSharedString(): String =
        """
        $title
        최초 작성일: $creationTime
        ${if (editTime == null) "" else "최근 수정일: $editTime"}
        내용:
        $content
        """.trimIndent()
}