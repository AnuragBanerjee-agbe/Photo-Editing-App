package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.database.VideoEditorDatabase
import com.example.data.entity.ClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.repository.VideoEditorRepository
import com.example.network.Content
import com.example.network.GeminiRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.util.ExporterUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class VideoEditorViewModel(
    application: Application,
    private val repository: VideoEditorRepository
) : AndroidViewModel(application) {

    // Projects list
    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active editing project state
    var activeProject by mutableStateOf<ProjectEntity?>(null)
        private set

    // Active project's clips flow
    private val _currentClips = MutableStateFlow<List<ClipEntity>>(emptyList())
    val currentClips = _currentClips.asStateFlow()

    // Playback state variables
    var isPlaying by mutableStateOf(false)
        private set
    var currentTimeMs by mutableStateOf(0L)
        private set
    var totalDurationMs by mutableStateOf(0L)
        private set

    // AI generation status
    var isAnalyzingWithAi by mutableStateOf(false)
        private set
    var aiStatusMessage by mutableStateOf("")
        private set
    var aiPromptInput by mutableStateOf("Create a 4-slide corporate keynote presentation outlining our new fintech mobile banking launch strategy.")

    // File writing / Export state
    var isExporting by mutableStateOf(false)
        private set
    var exportProgress by mutableStateOf(0f)
        private set
    var exportSuccessUri by mutableStateOf<Uri?>(null)
        private set

    // User customize fields
    var editClipIndex by mutableStateOf(-1)
        private set
    var editClipTitle by mutableStateOf("")
    var editClipSubtitles by mutableStateOf("")
    var editClipStyle by mutableStateOf("CORPORATE_SLIDE")
    var editClipDuration by mutableStateOf("4000")
    var editClipTransition by mutableStateOf("Cross Fade")

    private var playbackJob: Job? = null

    init {
        // Collect projects list to auto-select the first one or create an initial seed project
        viewModelScope.launch {
            allProjects.collect { projects ->
                if (projects.isEmpty()) {
                    seedDefaultProject()
                } else if (activeProject == null) {
                    selectProject(projects.first())
                }
            }
        }
    }

    private suspend fun seedDefaultProject() {
        val seedProj = ProjectEntity(
            name = "Project Highlight",
            description = "Quarterly Business Review & Product Pitch Slides."
        )
        val projId = repository.insertProject(seedProj)

        val seedClips = listOf(
            ClipEntity(
                projectId = projId, orderIndex = 0,
                title = "Fintech Horizon 2026",
                subtitle = "Digital Ledger Operations | Next-gen Retail APIs | Secure compliance model",
                clipStyle = "CORPORATE_SLIDE", durationMs = 4000, transitionType = "Cross Fade"
            ),
            ClipEntity(
                projectId = projId, orderIndex = 1,
                title = "Quarter Growth Curve",
                subtitle = "User acquisition up 145% | Revenue reaches $2.4M | Scale capacity extended",
                clipStyle = "METRICS_CHART", durationMs = 5000, transitionType = "Zoom Dynamic"
            ),
            ClipEntity(
                projectId = projId, orderIndex = 2,
                title = "Client Showcase Mobile",
                subtitle = "Custom widgets | Biometric lock | Instant cross-border remittances",
                clipStyle = "SOCIAL_PRODUCT", durationMs = 4000, transitionType = "Glitch Blur"
            )
        )
        repository.insertClips(seedClips)
    }

    fun selectProject(project: ProjectEntity) {
        activeProject = project
        pausePlayback()
        currentTimeMs = 0L

        // Collect new clips list
        viewModelScope.launch {
            repository.getClipsForProject(project.id).collect { clips ->
                _currentClips.value = clips
                totalDurationMs = clips.sumOf { it.durationMs }
                if (currentTimeMs > totalDurationMs) {
                    currentTimeMs = 0L
                }
            }
        }
    }

    fun createNewProject(name: String, desc: String) {
        viewModelScope.launch {
            val project = ProjectEntity(name = name, description = desc)
            val projId = repository.insertProject(project)
            val seedClip = ClipEntity(
                projectId = projId,
                orderIndex = 0,
                title = "Welcome to ${project.name}",
                subtitle = "Tap 'AI Presentation' to customize or write slides manually. | Double click to edit any slide.",
                clipStyle = "CORPORATE_SLIDE"
            )
            repository.insertClip(seedClip)
            val item = repository.getProjectById(projId)
            if (item != null) selectProject(item)
        }
    }

    fun deleteCurrentProject() {
        activeProject?.let { proj ->
            viewModelScope.launch {
                repository.deleteProject(proj.id)
                activeProject = null
            }
        }
    }

    fun updateColorGrade(presetName: String) {
        val proj = activeProject ?: return
        viewModelScope.launch {
            val updated = proj.copy(selectedFilter = presetName)
            repository.updateProject(updated)
            activeProject = updated
        }
    }

    // Playback control
    fun togglePlayback() {
        if (isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        isPlaying = true
        playbackJob = viewModelScope.launch {
            val startMs = currentTimeMs
            val startTime = System.currentTimeMillis() - startMs
            while (isPlaying) {
                val duration = currentClips.value.sumOf { it.durationMs }
                if (duration == 0L) {
                    isPlaying = false
                    break
                }
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= duration) {
                    currentTimeMs = 0L
                    isPlaying = false
                } else {
                    currentTimeMs = elapsed
                }
                delay(12) // Smooth frame tick (~80fps)
            }
        }
    }

    fun pausePlayback() {
        isPlaying = false
        playbackJob?.cancel()
    }

    fun seekTo(timeMs: Long) {
        currentTimeMs = timeMs.coerceIn(0L, totalDurationMs)
    }

    // Frame editor selections
    fun selectClipForEdit(clip: ClipEntity) {
        val index = currentClips.value.indexOfFirst { it.id == clip.id }
        if (index != -1) {
            editClipIndex = index
            editClipTitle = clip.title
            editClipSubtitles = clip.subtitle
            editClipStyle = clip.clipStyle
            editClipDuration = clip.durationMs.toString()
            editClipTransition = clip.transitionType
        }
    }

    fun clearEditSelection() {
        editClipIndex = -1
    }

    fun saveActiveEdit() {
        val index = editClipIndex
        val clips = currentClips.value
        if (index in clips.indices) {
            val clipToUpdate = clips[index]
            val parsedDuration = editClipDuration.toLongOrNull() ?: 4000L
            viewModelScope.launch {
                val updatedClip = clipToUpdate.copy(
                    title = editClipTitle,
                    subtitle = editClipSubtitles,
                    clipStyle = editClipStyle,
                    durationMs = parsedDuration,
                    transitionType = editClipTransition
                )
                repository.updateClip(updatedClip)
                clearEditSelection()
            }
        }
    }

    fun deleteClipAt(index: Int) {
        val clips = currentClips.value
        if (index in clips.indices) {
            viewModelScope.launch {
                repository.deleteClipById(clips[index].id)
            }
        }
    }

    fun addNewClip() {
        val proj = activeProject ?: return
        val currentSize = currentClips.value.size
        viewModelScope.launch {
            val newClip = ClipEntity(
                projectId = proj.id,
                orderIndex = currentSize,
                title = "New Presentation Scene",
                subtitle = "Custom core bullet | Tap fields below to custom edit details.",
                clipStyle = "CORPORATE_SLIDE"
            )
            repository.insertClip(newClip)
        }
    }

    // AI Choreographer with standard robust parsing
    fun choreographWithAi() {
        val proj = activeProject ?: return
        val prompt = aiPromptInput.trim()
        if (prompt.isEmpty()) return

        isAnalyzingWithAi = true
        aiStatusMessage = "Analyzing Project Scope..."

        viewModelScope.launch {
            try {
                aiStatusMessage = "Consulting Google Gemini..."
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                val sysPrompt = "You are a professional video choreographer and slides deck designer. Respond ONLY in raw JSON using this schema: {\"selectedFilter\":\"Choose: Corporate Clean, Warm Sunset, Cyber Tech Blue, Retro Cam, Teal & Orange, B&W Editorial\",\"clips\":[{\"title\":\"Slide Title\",\"subtitle\":\"bullet 1 | bullet 2 | bullet 3\",\"clipStyle\":\"Choose: CORPORATE_SLIDE, TEAM_PHOTO, METRICS_CHART, SOCIAL_PRODUCT, CAFE_HIGHLIGHT, SUNSET_SUMMARY\",\"durationMs\":4000,\"transitionType\":\"Choose: Cross Fade, Zoom Dynamic, Glitch Blur, Wipe Right, None\",\"transitionDurationMs\":800}]}"
                val userPrompt = "Generate high-polished video slides detailing this description: '$prompt'. Provide 3 to 5 slide items telling an outstanding professional story."

                val networkRequest = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
                    systemInstruction = Content(parts = listOf(Part(text = sysPrompt))),
                    generationConfig = com.example.network.GenerationConfig(
                        temperature = 0.6f,
                        responseMimeType = "application/json"
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, networkRequest)
                aiStatusMessage = "Parsing AI Metadata..."

                val jsonResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!jsonResponseText.isNullOrEmpty()) {
                    Log.d("VideoEditorVM", "Gemini response: $jsonResponseText")
                    
                    val cleanJson = cleanJsonResponse(jsonResponseText)
                    val mainObject = JSONObject(cleanJson)
                    val filter = mainObject.optString("selectedFilter", "Corporate Clean")
                    
                    // Update main project filter
                    val updatedProj = proj.copy(selectedFilter = filter)
                    repository.updateProject(updatedProj)
                    activeProject = updatedProj

                    // Load clips
                    val jsonClips = mainObject.getJSONArray("clips")
                    val parsedClips = mutableListOf<ClipEntity>()
                    for (i in 0 until jsonClips.length()) {
                        val obj = jsonClips.getJSONObject(i)
                        parsedClips.add(
                            ClipEntity(
                                projectId = proj.id,
                                orderIndex = i,
                                title = obj.optString("title", "Active Scene"),
                                subtitle = obj.optString("subtitle", "Engineered highlight point"),
                                clipStyle = obj.optString("clipStyle", "CORPORATE_SLIDE"),
                                durationMs = obj.optLong("durationMs", 4000L),
                                transitionType = obj.optString("transitionType", "Cross Fade"),
                                transitionDurationMs = obj.optLong("transitionDurationMs", 800L)
                            )
                        )
                    }

                    if (parsedClips.isNotEmpty()) {
                        repository.clearAndInsertClips(proj.id, parsedClips)
                        aiStatusMessage = "AI Choreography Applied!"
                    } else {
                        aiStatusMessage = "Fail: No clips returned."
                    }
                } else {
                    aiStatusMessage = "Error: Blank response."
                }
            } catch (e: Exception) {
                Log.e("VideoEditorVM", "AI Call failed", e)
                aiStatusMessage = "Error: Config failed, used default timeline."
            } finally {
                delay(1200)
                isAnalyzingWithAi = false
                aiStatusMessage = ""
            }
        }
    }

    private fun cleanJsonResponse(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.substringAfter("```json").substringBeforeLast("```").trim()
        } else if (str.startsWith("```")) {
            str = str.substringAfter("```").substringBeforeLast("```").trim()
        }
        return str
    }

    // Export simulation with real file download creation!
    fun exportProjectAndDownload() {
        val proj = activeProject ?: return
        val clips = currentClips.value
        if (clips.isEmpty()) return

        isExporting = true
        exportSuccessUri = null
        exportProgress = 0.0f

        viewModelScope.launch {
            try {
                // Simulate rendering frame by frame
                val steps = 10
                for (i in 1..steps) {
                    delay(300)
                    exportProgress = i.toFloat() / steps
                }

                // Call Exporter to write standard slide-presentation html to public directory
                val app = getApplication<Application>()
                val uri = ExporterUtil.exportProjectToHtml(app, proj, clips)
                exportProgress = 1.0f
                exportSuccessUri = uri

            } catch (e: Exception) {
                Log.e("VideoEditorVM", "Export failed", e)
            } finally {
                delay(800)
                isExporting = false
            }
        }
    }

    fun clearExportState() {
        exportSuccessUri = null
        exportProgress = 0f
    }
}

// Custom factory class to instantiate Room repo correctly
class VideoEditorViewModelFactory(
    private val application: Application,
    private val repository: VideoEditorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoEditorViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
