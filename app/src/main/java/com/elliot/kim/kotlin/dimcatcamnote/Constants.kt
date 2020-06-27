package com.elliot.kim.kotlin.dimcatcamnote

const val KEY_NOTE_ID = "key_id"
const val KEY_NOTE_TITLE = "key_title"
const val KEY_NOTE_CONTENT = "key_content"

const val KEY_APP_WIDGET_NOTE_EXIST = "key_app_widget_note_exist"
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
const val KEY_PASSWORD_CONFIRMED = "key_password_confirmed"

const val KEY_SORTING_CRITERIA = "key_sorting_criteria"

const val KEY_COLOR_TOOLBAR = "key_color_toolbar"
const val KEY_COLOR_BACKGROUND = "key_color_background"
const val KEY_COLOR_NOTE = "key_color_note_v2"
const val KEY_COLOR_APP_WIDGET_TITLE = "key_color_app_widget_title"
const val KEY_COLOR_APP_WIDGET_BACKGROUND = "key_color_app_widget_background"
const val KEY_COLOR_INLAY = "key_color_inlay"
const val KEY_SET_THEME_COLOR_CHECKED_RADIO_BUTTON_ID = "key_set_theme_color_checked_radio_button_id"
const val KEY_SET_NOTE_COLOR_CHECKED_RADIO_BUTTON_ID = "key_set_note_color_checked_radio_button_id"
const val KEY_SET_APP_WIDGET_COLOR_CHECKED_RADIO_BUTTON_ID = "key_set_app_widget_color_checked_radio_button_id"
const val KEY_SET_INLAY_COLOR_CHECKED_RADIO_BUTTON_ID = "key_set_inlay_color_checked_radio_button_id"
const val KEY_SET_FONT_CHECKED_RADIO_BUTTON_ID = "key_set_font_checked_radio_button_id"
const val KEY_FONT_ID = "key_font_id"
const val KEY_FONT_STYLE_ID = "key_font_style_id"
const val KEY_OPACITY = "key_opacity"
const val KEY_OPACITY_SEEK_BAR_PROGRESS = "key_opacity_seek_bar_progress"

const val DEFAULT_VALUE_NOTE_ID = -1

const val DEFAULT_FOLDER_ID = 0
const val DEFAULT_FOLDER_NAME = "모든 노트"

const val DEFAULT_FONT_ID = R.font.nanum_gothic_font_family

const val DEFAULT_HEX_OPACITY = 80
const val DEFAULT_SEEK_BAR_PROGRESS = 50

// Preferences
const val PREFERENCES_NAME_ALARM = "preferences_name_alarm"
const val PREFERENCES_SORTING_CRITERIA = "preference_sorting_criteria"
const val PREFERENCES_SET_COLOR = "preferences_set_color"
const val PREFERENCES_FOLDER = "preferences_folder_v3"
const val PREFERENCES_FONT = "preferences_font"
const val PREFERENCES_OPACITY = "preferences_opacity"
const val APP_WIDGET_PREFERENCES = "app_widget_preferences"

enum class SortingCriteria(val index: Int) {
    CREATION_TIME(0),
    EDIT_TIME(1),
    NAME(2)
}

enum class CurrentFragment {
    ALARM_FRAGMENT,
    CALENDAR_FRAGMENT,
    CAMERA_FRAGMENT,
    CONFIGURE_FRAGMENT,
    EDIT_FRAGMENT,
    PHOTO_FRAGMENT,
    WRITE_FRAGMENT
}

enum class NoteItem {
    TITLE,
    TIME,
    CONTENT
}

const val PATTERN_YYYY_MM_dd = "yyyy년 M월 d일"
const val PATTERN_UP_TO_SECONDS = "yyyy년 M월 d일 a hh:mm:ss"
const val PATTERN_UP_TO_MINUTES = "M월 d일 a h시 m분"

// action widget
const val ACTION_APP_WIDGET_ATTACHED = "action_app_widget_attached"

// Alarm notification click action.
const val ACTION_ALARM_NOTIFICATION_CLICKED = "action_alarm_notification_clicked"

// 유틸리티 클래스나 파일 만들것.
fun dateToText(year: Int, month: Int, dayOfMonth: Int) = "${year}년 ${month + 1}월 ${dayOfMonth}일"