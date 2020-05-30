package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import com.elliot.kim.kotlin.dimcatcamnote.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.NoteAdapter

// The password-related dialog fragments require additional parameters to be entered,
// so it is not managed by the DialogFragmentManager,
// and is instantiated and used individually.

class DialogFragmentManager(private val activity: MainActivity,
                            private val folderAdapter: FolderAdapter,
                            private val noteAdapter: NoteAdapter) {

    private val tag = "DialogFragmentManager"

    fun showDialogFragment(dialogFragment: DialogFragments) {

        when (dialogFragment) {
            DialogFragments.ADD_FOLDER -> AddFolderDialogFragment(folderAdapter)
                .show(activity.fragmentManager, tag)
            DialogFragments.ADD_TO_CALENDER -> AddToCalendarDialogFragment(noteAdapter.selectedNote!!)
                .show(activity.fragmentManager, tag)
            DialogFragments.FOLDER_OPTIONS -> FolderOptionsDialogFragment(folderAdapter, noteAdapter)
                .show(activity.fragmentManager, tag)
            DialogFragments.MORE_OPTIONS -> MoreOptionsDialogFragment(noteAdapter)
                .show(activity.fragmentManager, tag)
            DialogFragments.SORT -> SortDialogFragment(noteAdapter)
                .show(activity.fragmentManager, tag)
        }
    }
}