package com.example.pocketsafe.data

import android.net.Uri
import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Uri converters
    @TypeConverter
    fun fromUriString(value: String?): Uri? {
        return if (value == null) null else Uri.parse(value)
    }

    @TypeConverter
    fun toUriString(uri: Uri?): String? {
        return uri?.toString()
    }

    // List<String> converters
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }
} 