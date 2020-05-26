package com.elliot.kim.kotlin.dimcatcamnote.dialogs

import android.app.Activity

open class CustomDialog(activity: Activity) {
    open fun buildDialog() {val b = 1}
    open fun show() { buildDialog() }
}