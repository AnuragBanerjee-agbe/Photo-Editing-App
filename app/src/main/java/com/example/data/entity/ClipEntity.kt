package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clips",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class ClipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val projectId: Long,
    val orderIndex: Int,
    val title: String,
    val subtitle: String = "", // Holds bullet items or speaker notes
    val clipStyle: String = "CORPORATE_SLIDE", // Visual theme identifier
    val durationMs: Long = 4000,
    val transitionType: String = "Cross Fade", // "None", "Cross Fade", "Zoom Dynamic", "Glitch Blur", "Wipe Right"
    val transitionDurationMs: Long = 800,
    val brightness: Float = 1.0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f
)
