package com.elliot.kim.kotlin.dimcatcamnote

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.lang.reflect.Type

class Converters {
    @TypeConverter
    fun arrayListToJson(value: Array<Int>) = Gson().toJson(value)

    @TypeConverter
    fun jsonToArrayList(value: String) = Gson().fromJson(value, Array<Int>::class.java)
}