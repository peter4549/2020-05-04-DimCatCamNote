package com.elliot.kim.kotlin.dimcatcamnote.database

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun arrayListToJson(value: Array<Int>) = Gson().toJson(value)

    @TypeConverter
    fun jsonToArrayList(value: String) = Gson().fromJson(value, Array<Int>::class.java)
}