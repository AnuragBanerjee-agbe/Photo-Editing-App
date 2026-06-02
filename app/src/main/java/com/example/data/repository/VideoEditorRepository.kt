package com.example.data.repository

import com.example.data.dao.VideoEditorDao
import com.example.data.entity.ClipEntity
import com.example.data.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

class VideoEditorRepository(private val dao: VideoEditorDao) {
    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    suspend fun getProjectById(id: Long): ProjectEntity? {
        return dao.getProjectById(id)
    }

    suspend fun insertProject(project: ProjectEntity): Long {
        return dao.insertProject(project)
    }

    suspend fun updateProject(project: ProjectEntity) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(projectId: Long) {
        dao.deleteProjectById(projectId)
    }

    fun getClipsForProject(projectId: Long): Flow<List<ClipEntity>> {
        return dao.getClipsForProject(projectId)
    }

    suspend fun getClipsForProjectSync(projectId: Long): List<ClipEntity> {
        return dao.getClipsForProjectSync(projectId)
    }

    suspend fun insertClips(clips: List<ClipEntity>) {
        dao.insertClips(clips)
    }

    suspend fun insertClip(clip: ClipEntity): Long {
        return dao.insertClip(clip)
    }

    suspend fun updateClip(clip: ClipEntity) {
        dao.updateClip(clip)
    }

    suspend fun deleteClipById(clipId: Long) {
        dao.deleteClipById(clipId)
    }

    suspend fun clearAndInsertClips(projectId: Long, clips: List<ClipEntity>) {
        dao.deleteClipsForProject(projectId)
        dao.insertClips(clips)
    }
}
