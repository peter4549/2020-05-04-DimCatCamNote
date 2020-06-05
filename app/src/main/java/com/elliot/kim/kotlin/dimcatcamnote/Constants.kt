package com.elliot.kim.kotlin.dimcatcamnote

const val KEY_NOTE_ID = "key_id"
const val KEY_NOTE_TITLE = "key_title"
const val KEY_NOTE_CONTENT = "key_content"

const val KEY_APP_WIDGET_NOTE_ID = "key_app_widget_note_id"
const val KEY_APP_WIDGET_NOTE_TITLE = "key_app_widget_note_title"
const val KEY_APP_WIDGET_NOTE_CONTENT = "key_app_widget_note_content"
const val KEY_APP_WIDGET_NOTE_URI = "key_app_widget_note_uri"
const val KEY_APP_WIDGET_NOTE_CREATION_TIME = "key_app_widget_note_creation_time"
const val KEY_APP_WIDGET_NOTE_EDIT_TIME = "key_app_widget_note_edit_time"
const val KEY_APP_WIDGET_NOTE_ALARM_TIME = "key_app_widget_note_alarm_time"
const val KEY_APP_WIDGET_NOTE_IS_DONE = "key_app_widget_note_is_done"
const val KEY_APP_WIDGET_NOTE_IS_LOCKED = "key_app_widget_note_is_locked"
const val KEY_APP_WIDGET_NOTE_PASSWORD = "key_app_widget_note_password"
const val KEY_APP_WIDGET_IDS = "key_app_widget_ids"

const val KEY_SORTING_CRITERIA = "key_sorting_criteria"

const val DEFAULT_VALUE_NOTE_ID = -1

const val DEFAULT_FOLDER_ID = 0
const val DEFAULT_FOLDER_NAME = "모든 노트"

const val PREFERENCES_NAME_ALARM = "preferences_name_alarm"
const val PREFERENCES_SORTING_CRITERIA = "preference_sorting_criteria"

enum class SortingCriteria(val index: Int) {
    CREATION_TIME(0),
    EDIT_TIME(1),
    NAME(2)
}


enum class CurrentFragment {
    ALARM_FRAGMENT,
    CALENDAR_FRAGMENT,
    CAMERA_FRAGMENT,
    EDIT_FRAGMENT,
    PHOTO_FRAGMENT,
    WRITE_FRAGMENT
}

const val PATTERN_YYYY_MM_dd = "yyyy년 MM월 dd일"
const val PATTERN_UP_TO_SECONDS = "yyyy-MM-dd-a-hh:mm:ss"

// action widget
const val ACTION_APP_WIDGET_ATTACHED = "action_app_widget_attached"

// 유틸리티 클래스나 파일 만들것.
fun dateToText(year: Int, month: Int, dayOfMonth: Int) = "${year}년 ${month + 1}월 ${dayOfMonth}일"