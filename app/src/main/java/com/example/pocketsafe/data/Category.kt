package com.example.pocketsafe.data

import androidx.room.*

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val monthlyAmount: Double = 0.0,
    @TypeConverters(IconTypeConverter::class)
    val iconType: IconType,
    val icon: String? = null,
    val color: Int? = null
)

class IconTypeConverter {
    @TypeConverter
    fun fromIconType(iconType: IconType): String = iconType.name

    @TypeConverter
    fun toIconType(value: String): IconType = IconType.valueOf(value)
}
