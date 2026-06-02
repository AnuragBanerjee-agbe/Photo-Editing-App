package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.VideoEditorDao
import com.example.data.entity.ClipEntity
import com.example.data.entity.ProjectEntity

@Database(entities = [ProjectEntity::class, ClipEntity::class], version = 1, exportSchema = false)
abstract class VideoEditorDatabase : RoomDatabase() {
    abstract fun videoEditorDao(): VideoEditorDao

    companion object {
        @Volatile
        private var INSTANCE: VideoEditorDatabase? = null

        fun getDatabase(context: Context): VideoEditorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VideoEditorDatabase::class.java,
                    "video_editor_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
