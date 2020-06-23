package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.data.Note

class MoreOptionsDialogFragment(private val noteAdapter: NoteAdapter) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireActivity() as MainActivity)
        dialog.setContentView(R.layout.dialog_fragment_more_options)

        val textView = dialog.findViewById<TextView>(R.id.text_view_title)
        textView.text = noteAdapter.selectedNote!!.title
        val textViewAddToCalendar =
            dialog.findViewById<TextView>(R.id.text_view_add_to_calendar)
        val textViewMoveToFolder =
            dialog.findViewById<TextView>(R.id.text_view_move_to_folder)
        val textViewLock =
            dialog.findViewById<TextView>(R.id.text_view_lock)
        val textViewShare =
            dialog.findViewById<TextView>(R.id.text_view_share)
        val textViewAddToStatusBar =
            dialog.findViewById<TextView>(R.id.text_view_add_to_status_bar)

        textView.setBackgroundColor(MainActivity.toolbarColor)
        dialog.findViewById<LinearLayout>(R.id.more_options_container)
            .setBackgroundColor(MainActivity.backgroundColor)

        textView.adjustDialogTitleTextSize(MainActivity.fontId)
        textViewAddToCalendar.adjustDialogItemTextSize(MainActivity.fontId)
        textViewMoveToFolder.adjustDialogItemTextSize(MainActivity.fontId)
        textViewLock.adjustDialogItemTextSize(MainActivity.fontId)
        textViewShare.adjustDialogItemTextSize(MainActivity.fontId)
        textViewAddToStatusBar.adjustDialogItemTextSize(MainActivity.fontId)

        textView.typeface = MainActivity.font
        textViewAddToCalendar.typeface = MainActivity.font
        textViewMoveToFolder.typeface = MainActivity.font
        textViewLock.typeface = MainActivity.font
        textViewShare.typeface = MainActivity.font
        textViewAddToStatusBar.typeface = MainActivity.font

        textViewAddToCalendar.setOnClickListener {
            AddToCalendarDialogFragment(noteAdapter.selectedNote!!)
                .show((requireActivity() as MainActivity).fragmentManager, tag)
            dialog.dismiss()
        }

        textViewMoveToFolder.setOnClickListener {
            (requireActivity() as MainActivity).showDialogFragment(DialogFragments.MOVE_TO_FOLDER)
            dialog.dismiss()
        }

        textViewLock.setOnClickListener {
            SetPasswordDialogFragment(noteAdapter)
                .show((requireActivity() as MainActivity).fragmentManager, tag)
            dialog.dismiss()
        }

        textViewShare.setOnClickListener {
            MainActivity.share(requireContext(), noteAdapter.selectedNote!!)
            dialog.dismiss()
        }

        textViewAddToStatusBar.setOnClickListener {
            addToStatusBar(noteAdapter.selectedNote!!)
            dialog.dismiss()
        }

        return dialog
    }

    private fun addToStatusBar(note: Note) {
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, EditActivity::class.java)
        val id = note.id
        val title = note.title
        val content = note.content

        notificationIntent.action = ACTION_ALARM_NOTIFICATION_CLICKED + id
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        notificationIntent.putExtra(KEY_NOTE_ID, id)

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            notificationIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)

            builder.setSmallIcon(R.drawable.ic_notification_120px)
            channel.description = CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(channel)
        } else builder.setSmallIcon(R.mipmap.ic_notification_120px)

        builder.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setContentText(content)
            .setContentInfo(CONTENT_INFO)
            .setContentIntent(pendingIntent)
        notificationManager.notify(id, builder.build())

        (requireActivity() as MainActivity).showToast("상태바에 등록되었습니다.")
    }

    companion object {
        private const val CHANNEL_ID = "default"
        private const val CHANNEL_NAME = "com_duke_elliot_kim_kotlin_cat_note"
        private const val CHANNEL_DESCRIPTION = "dim_cat_note_notification_channel"
        private const val CONTENT_INFO = "none"
    }
}