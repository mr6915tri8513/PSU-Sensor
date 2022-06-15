package com.example.psusensor.theme

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theme_table")
class Theme(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val iconUri: String?,
    val backgroundColor: Int,
    val ledColor: Int,
    val textColor: Int
) {
    companion object {
        val Black = Color.rgb(0, 0, 0)
        val Blue = Color.rgb(51, 204, 255)
        val LightGreen = Color.rgb(51, 255, 51)
        val White = Color.rgb(255, 255, 255)
        val AquaBlue = Color.rgb(60, 236, 255)
        val AquaColor = Color.rgb(255, 106, 229)
    }

    constructor(name: String, iconUri: String?, backgroundColor: Int, ledColor: Int, textColor: Int): this(
        0, name, iconUri, backgroundColor, ledColor, textColor
    )
}