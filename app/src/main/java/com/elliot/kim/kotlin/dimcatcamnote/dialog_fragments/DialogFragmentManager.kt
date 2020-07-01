package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments


import androidx.appcompat.widget.Toolbar
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter

// The password-related dialog fragments require additional parameters to be entered,
// so it is not managed by the DialogFragmentManager,
// and is instantiated and used individually.

class DialogFragmentManager(private val activity: MainActivity,
                            private val folderAdapter: FolderAdapter,
                            private val noteAdapter: NoteAdapter

) {
    private val tag = "DialogFragmentManager"

    fun showDialogFragment(dialogFragment: DialogFragments, toolbar: Toolbar? = null) {

        when (dialogFragment) {
            DialogFragments.ADD_FOLDER -> AddFolderDialogFragment(folderAdapter)
                .show(activity.fragmentManager, tag)
            DialogFragments.ADD_TO_CALENDER -> AddToCalendarDialogFragment(noteAdapter.selectedNote!!)
                .show(activity.fragmentManager, tag)
            DialogFragments.CONFIRM_CLEAR -> ConfirmClearDialogFragment()
                .show(activity.fragmentManager, tag)
            DialogFragments.CONFIRM_DELETE -> ConfirmDeleteDialogFragment(noteAdapter.selectedNote!!)
                .show(activity.fragmentManager, tag)
            DialogFragments.CONFIRM_DELETE_FOLDER -> ConfirmDeleteFolderDialogFragment(folderAdapter)
                .show(activity.fragmentManager, tag)
            DialogFragments.MOVE_TO_FOLDER -> MoveToFolderDialogFragment(folderAdapter, noteAdapter)
                .show(activity.fragmentManager, tag)
            DialogFragments.MORE_OPTIONS -> MoreOptionsDialogFragment(noteAdapter)
                .show(activity.fragmentManager, tag)
            DialogFragments.SET_APP_WIDGET_COLOR -> SetAppWidgetColorDialogFragment()
                .show(activity.fragmentManager, tag)
            DialogFragments.SORT -> SortDialogFragment(noteAdapter)
                .show(activity.fragmentManager, tag)
            DialogFragments.SET_THEME_COLOR -> SetThemeColorDialogFragment(toolbar)
                .show(activity.fragmentManager, tag)
            DialogFragments.SET_NOTE_COLOR -> SetNoteColorDialogFragment()
                .show(activity.fragmentManager, tag)
            DialogFragments.SET_INLAY_COLOR -> SetInlayColorDialogFragment()
                .show(activity.fragmentManager, tag)
            DialogFragments.SET_FONT -> SetFontDialogFragment(toolbar)
                .show(activity.fragmentManager, tag)
            DialogFragments.SET_FONT_COLOR -> SetTextColorDialogFragment()
                .show(activity.fragmentManager, tag)
            DialogFragments.SET_YEAR_MONTH -> SetYearMonthDialogFragment()
                .show(activity.fragmentManager, tag)
        }
    }
}