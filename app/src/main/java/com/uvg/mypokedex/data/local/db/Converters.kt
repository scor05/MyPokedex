package com.uvg.mypokedex.data.local.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTypes(value: String): List<String> = if (value.isBlank()) emptyList() else value.split("|")

    @TypeConverter
    fun toTypes(list: List<String>): String = list.joinToString("|")

    @TypeConverter
    fun fromStats(value: String): Map<String, Int> =
        if (value.isBlank()) emptyMap() else value.split(",").associate {
            val p = it.split(":")
            p[0] to p[1].toInt()
        }

    @TypeConverter
    fun toStats(map: Map<String, Int>): String =
        map.entries.joinToString(",") { "${it.key}:${it.value}"}
}