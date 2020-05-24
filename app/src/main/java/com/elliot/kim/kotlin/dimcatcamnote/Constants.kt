package com.elliot.kim.kotlin.dimcatcamnote

const val KEY_NOTE_ID = "key_id"
const val KEY_NOTE_TITLE = "key_title"
const val KEY_NOTE_CONTENT = "key_content"

const val DEFAULT_VALUE_NOTE_ID = -1

const val DEFAULT_FOLDER_ID = 0
const val DEFAULT_FOLDER_NAME = "모든 노트"

const val PREFERENCES_NAME_ALARM = "preferences_name_alarm"

enum class CurrentFragment {
    ALARM_FRAGMENT,
    CAMERA_FRAGMENT,
    EDIT_FRAGMENT,
    WRITE_FRAGMENT
}