package com.elliot.kim.kotlin.dimcatcamnote

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note (var title: String?,
                 var creationTime: Long,
                 var uri: String?) : Parcelable {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
    var content: String? = null
    var editTime: Long? = null
    var alarmTime: Long? = null
    var isDone: Boolean = false

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readLong(),
        parcel.readString()
    ) {
        id = parcel.readInt()
        content = parcel.readString()
        editTime = parcel.readValue(Long::class.java.classLoader) as? Long
        alarmTime = parcel.readValue(Long::class.java.classLoader) as? Long
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeLong(creationTime)
        parcel.writeString(uri)
        parcel.writeInt(id)
        parcel.writeString(content)
        parcel.writeValue(editTime)
        parcel.writeValue(alarmTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }
}