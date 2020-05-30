package com.elliot.kim.kotlin.dimcatcamnote

const val KEY_NOTE_ID = "key_id"
const val KEY_NOTE_TITLE = "key_title"
const val KEY_NOTE_CONTENT = "key_content"

const val KEY_APP_WIDGET_NOTE_ID = ""
const val KEY_APP_WIDGET_NOTE_TITLE = ""
const val KEY_APP_WIDGET_NOTE_CONTENT = ""

const val DEFAULT_VALUE_NOTE_ID = -1

const val DEFAULT_FOLDER_ID = 0
const val DEFAULT_FOLDER_NAME = "모든 노트"

const val PREFERENCES_NAME_ALARM = "preferences_name_alarm"

enum class CurrentFragment {
    ALARM_FRAGMENT,
    CAMERA_FRAGMENT,
    EDIT_FRAGMENT,
    PHOTO_FRAGMENT,
    WRITE_FRAGMENT
}

enum class SortBy {
    EDIT_TIME,
    CREATION_TIME,
    NAME
}

const val PATTERN_YYYY_MM_dd = "yyyy년 MM월 dd일"

// action widget
const val ACTION_APP_WIDGET_ATTACHED = "action_app_widget_attached"

// 유틸리티 클래스나 파일 만들것.
fun dateToText(year: Int, month: Int, dayOfMonth: Int) = "${year}년 ${month + 1}월 ${dayOfMonth}일"