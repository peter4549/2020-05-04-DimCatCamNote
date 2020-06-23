/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elliot.kim.kotlin.dimcatcamnote

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.DisplayCutout
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager


/** Combination of all flags required to put activity into immersive mode */
const val FLAGS_FULLSCREEN =
        View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

/** Milliseconds used for UI animations */
const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_SLOW_MILLIS = 100L

/**
 * Simulate a button click, including a small delay while it is being pressed to trigger the
 * appropriate animations.
 */
fun ImageButton.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
    performClick()
    isPressed = true
    invalidate()
    postDelayed({
        invalidate()
        isPressed = false
    }, delay)
}

/** Pad this view with the insets provided by the device cutout (i.e. notch) */
@RequiresApi(Build.VERSION_CODES.P)
fun View.padWithDisplayCutout() {

    /** Helper method that applies padding from cutout's safe insets */
    fun doPadding(cutout: DisplayCutout) = setPadding(
            cutout.safeInsetLeft,
            cutout.safeInsetTop,
            cutout.safeInsetRight,
            cutout.safeInsetBottom)

    // Apply padding using the display cutout designated "safe area"
    rootWindowInsets?.displayCutout?.let { doPadding(it) }

    // Set a listener for window insets since view.rootWindowInsets may not be ready yet
    setOnApplyWindowInsetsListener { _, insets ->
        insets.displayCutout?.let { doPadding(it) }
        insets
    }
}

/** Same as [AlertDialog.show] but setting immersive mode in the dialog's window */
fun AlertDialog.showImmersive() {
    // Set the dialog to not focusable
    window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

    // Make sure that the dialog's window is in full screen
    window?.decorView?.systemUiVisibility = FLAGS_FULLSCREEN

    // Show the dialog while still in immersive mode
    show()

    // Set the dialog to focusable again
    window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
}

val smallFontFamilies = listOf(R.font.nanum_brush_font_family, R.font.nanum_pen_font_family)
val middleFontFamilies = listOf(R.font.bmyeonsung_font_family)

fun TextView.adjustDialogTitleTextSize(fontId: Int) {
    when(fontId) {
        in smallFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_DIALOG_TITLE_TEXT_SIZE + 6)
        in middleFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_DIALOG_TITLE_TEXT_SIZE + 2)
        else -> this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_DIALOG_TITLE_TEXT_SIZE)
    }
}

fun TextView.adjustSpinnerItemTextSize(fontId: Int) {
    when(fontId) {
        in smallFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_SPINNER_ITEM_TEXT_SIZE + 6)
        in middleFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_SPINNER_ITEM_TEXT_SIZE + 2)
        else -> this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_SPINNER_ITEM_TEXT_SIZE)
    }
}

fun TextView.adjustDialogItemTextSize(fontId: Int, smallText: Boolean = false) {
    var itemTextSize = BASIC_DIALOG_ITEM_TEXT_SIZE
    if (smallText)
        itemTextSize = SMALL_DIALOG_ITEM_TEXT_SIZE

    when(fontId) {
        in smallFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTextSize + 6)
        in middleFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTextSize + 2)
        else -> this.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTextSize)
    }
}

fun TextView.adjustNoteTextSize(fontId: Int, item: NoteItem) {
    var itemTextSize = NOTE_TITLE_TEXT_SIZE
    when (item) {
        NoteItem.TIME -> itemTextSize = NOTE_TIME_TEXT_SIZE
        NoteItem.CONTENT -> itemTextSize = NOTE_CONTENT_TEXT_SIZE
        else -> {}
    }

    when(fontId) {
        in smallFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTextSize + 4)
        in middleFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTextSize + 2)
        else -> this.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTextSize)
    }
}

fun EditText.adjustDialogInputTextSize(fontId: Int) {
    when(fontId) {
        in smallFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_DIALOG_INPUT_TEXT_SIZE + 6)
        in middleFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_DIALOG_INPUT_TEXT_SIZE + 2)
        else -> this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_DIALOG_INPUT_TEXT_SIZE)
    }
}

fun Button.adjustDialogButtonTextSize(fontId: Int) {
    when(fontId) {
        in smallFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_DIALOG_BUTTON_TEXT_SIZE + 6)
        in middleFontFamilies ->
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_DIALOG_BUTTON_TEXT_SIZE + 2)
        else -> this.setTextSize(TypedValue.COMPLEX_UNIT_SP, BASIC_DIALOG_BUTTON_TEXT_SIZE)
    }
}

class LinearLayoutManagerWrapper: LinearLayoutManager {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) :
            super(context, orientation, reverseLayout) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)
    override fun supportsPredictiveItemAnimations(): Boolean { return false }
}

fun Drawable.setColorFilter(color: Int, mode: Mode = Mode.SRC_ATOP) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        colorFilter = BlendModeColorFilter(color, mode.getBlendMode())
    } else {
        @Suppress("DEPRECATION")
        setColorFilter(color, mode.getPorterDuffMode())
    }
}

// This class is needed to call the setColorFilter
// with different BlendMode on older API (before 29).
enum class Mode {
    CLEAR,
    SRC,
    DST,
    SRC_OVER,
    DST_OVER,
    SRC_IN,
    DST_IN,
    SRC_OUT,
    DST_OUT,
    SRC_ATOP,
    DST_ATOP,
    XOR,
    DARKEN,
    LIGHTEN,
    MULTIPLY,
    SCREEN,
    ADD,
    OVERLAY;

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getBlendMode(): BlendMode =
        when (this) {
            CLEAR -> BlendMode.CLEAR
            SRC -> BlendMode.SRC
            DST -> BlendMode.DST
            SRC_OVER -> BlendMode.SRC_OVER
            DST_OVER -> BlendMode.DST_OVER
            SRC_IN -> BlendMode.SRC_IN
            DST_IN -> BlendMode.DST_IN
            SRC_OUT -> BlendMode.SRC_OUT
            DST_OUT -> BlendMode.DST_OUT
            SRC_ATOP -> BlendMode.SRC_ATOP
            DST_ATOP -> BlendMode.DST_ATOP
            XOR -> BlendMode.XOR
            DARKEN -> BlendMode.DARKEN
            LIGHTEN -> BlendMode.LIGHTEN
            MULTIPLY -> BlendMode.MULTIPLY
            SCREEN -> BlendMode.SCREEN
            ADD -> BlendMode.PLUS
            OVERLAY -> BlendMode.OVERLAY
        }

    fun getPorterDuffMode(): PorterDuff.Mode =
        when (this) {
            CLEAR -> PorterDuff.Mode.CLEAR
            SRC -> PorterDuff.Mode.SRC
            DST -> PorterDuff.Mode.DST
            SRC_OVER -> PorterDuff.Mode.SRC_OVER
            DST_OVER -> PorterDuff.Mode.DST_OVER
            SRC_IN -> PorterDuff.Mode.SRC_IN
            DST_IN -> PorterDuff.Mode.DST_IN
            SRC_OUT -> PorterDuff.Mode.SRC_OUT
            DST_OUT -> PorterDuff.Mode.DST_OUT
            SRC_ATOP -> PorterDuff.Mode.SRC_ATOP
            DST_ATOP -> PorterDuff.Mode.DST_ATOP
            XOR -> PorterDuff.Mode.XOR
            DARKEN -> PorterDuff.Mode.DARKEN
            LIGHTEN -> PorterDuff.Mode.LIGHTEN
            MULTIPLY -> PorterDuff.Mode.MULTIPLY
            SCREEN -> PorterDuff.Mode.SCREEN
            ADD -> PorterDuff.Mode.ADD
            OVERLAY -> PorterDuff.Mode.OVERLAY
        }
}