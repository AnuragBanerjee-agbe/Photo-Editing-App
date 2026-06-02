package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ClipEntity
import com.example.data.entity.ProjectEntity
import com.example.ui.style.ColorGrading
import com.example.ui.style.RenderClipVisual
import com.example.ui.theme.*
import com.example.ui.viewmodel.VideoEditorViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

// GPU-accelerated backwards compatible color matrix filter wrapper
fun Modifier.applyColorGrade(activeGradeName: String): Modifier = this.drawWithContent {
    val matrix = ColorGrading.getByName(activeGradeName).matrix
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            colorFilter = ColorFilter.colorMatrix(matrix)
        }
        canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
        drawContent()
        canvas.restore()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(viewModel: VideoEditorViewModel) {
    val context = LocalContext.current
    val allProjects by viewModel.allProjects.collectAsState()
    val activeProject = viewModel.activeProject
    val clips by viewModel.currentClips.collectAsState()

    var activeTab by remember { mutableStateOf("AI_ASSISTANT") } // "AI_ASSISTANT", "COLOR_GRADE", "TRANSITIONS", "SETUP"
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var newProjName by remember { mutableStateOf("") }
    var newProjDesc by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (activeProject != null) activeProject.name else "Q4 Strategy Highlight",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (activeProject != null) "Saved 2m ago" else "No active project",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showNewProjectDialog = true }
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = "New Project", tint = PrimaryPurple)
                    }
                    if (activeProject != null) {
                        Button(
                            onClick = { viewModel.exportProjectAndDownload() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple,
                                contentColor = OnPrimaryPurple
                            ),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeProject == null) {
                // Empty view, select project
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.MovieCreation,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Create or Select a Studio Project",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Build multi-slide presentations, team photo highlights, and products displays with Gemini AI.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                    )
                    
                    if (allProjects.isNotEmpty()) {
                        Text(
                            text = "Or choose an existing library project:",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(allProjects) { _, proj ->
                                Card(
                                    onClick = { viewModel.selectProject(proj) },
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    border = BorderStroke(1.dp, OutlineGrey),
                                    modifier = Modifier.width(130.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(proj.name, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(proj.selectedFilter, color = PrimaryPurple, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showNewProjectDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple,
                            contentColor = OnPrimaryPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create Fresh Studio", color = OnPrimaryPurple, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Workspace view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Monitor View with transitions applied
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .aspectRatio(1.77f) // Cinematic 16:9 aspect ratio
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, OutlineGrey, RoundedCornerShape(24.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        VideoPreviewMonitor(
                            clips = clips,
                            currentTimeMs = viewModel.currentTimeMs,
                            activeGradeName = activeProject.selectedFilter
                        )
                    }

                    // Seek controls bar
                    PlaybackControlBar(viewModel)

                    // Track selector timeline scrollable
                    TimelineTrackRow(
                        clips = clips,
                        currentTimeMs = viewModel.currentTimeMs,
                        onSeekTo = { viewModel.seekTo(it) },
                        onSelectSelect = { viewModel.selectClipForEdit(it) },
                        onDeleteClip = { viewModel.deleteClipAt(it) },
                        onAddClip = { viewModel.addNewClip() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controller deck Tab selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundDark)
                            .border(BorderStroke(1.dp, OutlineGrey))
                            .padding(vertical = 4.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TabHeaderButton(
                            title = "🤖 AI Assist",
                            isActive = activeTab == "AI_ASSISTANT",
                            onClick = { activeTab = "AI_ASSISTANT" }
                        )
                        TabHeaderButton(
                            title = "🎨 Color Grade",
                            isActive = activeTab == "COLOR_GRADE",
                            onClick = { activeTab = "COLOR_GRADE" }
                        )
                        TabHeaderButton(
                            title = "🎬 Transitions",
                            isActive = activeTab == "TRANSITIONS",
                            onClick = { activeTab = "TRANSITIONS" }
                        )
                        TabHeaderButton(
                            title = "⚙️ Studio Setup",
                            isActive = activeTab == "SETUP",
                            onClick = { activeTab = "SETUP" }
                        )
                    }

                    // Content panel corresponding to active tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundDark)
                            .padding(16.dp)
                    ) {
                        when (activeTab) {
                            "AI_ASSISTANT" -> AiAssistantTab(viewModel)
                            "COLOR_GRADE" -> ColorGradeTab(viewModel, activeProject)
                            "TRANSITIONS" -> TransitionsTab(viewModel)
                            "SETUP" -> SetupProjectTab(viewModel, allProjects)
                        }
                    }
                }
            }

            // Floating Active Clip Editor overlay when a clip is selected
            if (viewModel.editClipIndex != -1) {
                ClipEditorOverlay(viewModel)
            }

            // Export Progress overlay Dialog
            if (viewModel.isExporting) {
                ExportingProgressOverlay(viewModel.exportProgress)
            }

            // Successfully Exported Dialog
            viewModel.exportSuccessUri?.let { uri ->
                ExportSuccessDialog(
                    fileName = activeProject?.name ?: "Presentation",
                    uri = uri,
                    onDismiss = { viewModel.clearExportState() }
                )
            }
        }
    }

    // New Project creation Dialog
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Project", color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newProjName,
                        onValueChange = { newProjName = it },
                        label = { Text("Studio Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = OutlineGrey,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newProjDesc,
                        onValueChange = { newProjDesc = it },
                        label = { Text("Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = OutlineGrey,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjName.trim().isNotEmpty()) {
                            viewModel.createNewProject(newProjName, newProjDesc)
                            showNewProjectDialog = false
                            newProjName = ""
                            newProjDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple,
                        contentColor = OnPrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Initialize", color = OnPrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel", color = PrimaryPurple)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// Interactive Live Video Monitor applying real time shaders / transition matrices
@Composable
fun VideoPreviewMonitor(
    clips: List<ClipEntity>,
    currentTimeMs: Long,
    activeGradeName: String
) {
    if (clips.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Empty Studio Timeline", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
        }
        return
    }

    val totalMs = clips.sumOf { it.durationMs }
    var activeIdx by remember(currentTimeMs) { mutableStateOf(0) }
    var nextIdx by remember(currentTimeMs) { mutableStateOf(0) }
    var transitionProgress by remember(currentTimeMs) { mutableStateOf(0f) }
    var isTransitionRunning by remember(currentTimeMs) { mutableStateOf(false) }

    // State calculation
    LaunchedEffect(currentTimeMs, clips) {
        if (totalMs > 0 && clips.isNotEmpty()) {
            var elapsed = 0L
            for (i in clips.indices) {
                val clip = clips[i]
                val clipStart = elapsed
                val clipEnd = elapsed + clip.durationMs
                val transitionStart = clipEnd - clip.transitionDurationMs

                if (currentTimeMs in clipStart until clipEnd) {
                    activeIdx = i
                    // Determine if transition applies to next
                    if (currentTimeMs >= transitionStart && i < clips.size - 1 && clip.transitionType != "None") {
                        isTransitionRunning = true
                        nextIdx = i + 1
                        val duration = clip.transitionDurationMs
                        transitionProgress = if (duration > 0) {
                            (currentTimeMs - transitionStart).toFloat() / duration
                        } else 0f
                    } else {
                        isTransitionRunning = false
                    }
                    break
                }
                elapsed = clipEnd
            }
        }
    }

    val activeClip = clips.getOrNull(activeIdx) ?: clips[0]

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isTransitionRunning) {
            // Standard slide rendering
            RenderClipVisual(
                clipStyle = activeClip.clipStyle,
                title = activeClip.title,
                subtitle = activeClip.subtitle,
                modifier = Modifier
                    .fillMaxSize()
                    .applyColorGrade(activeGradeName)
            )
        } else {
            // Combined active transition rendering
            val nextClip = clips[nextIdx]
            val transitionType = activeClip.transitionType

            Box(modifier = Modifier.fillMaxSize()) {
                when (transitionType) {
                    "Cross Fade" -> {
                        // Dissolve layer outgoing decreases opacity, incoming increases
                        RenderClipVisual(
                            clipStyle = activeClip.clipStyle,
                            title = activeClip.title,
                            subtitle = activeClip.subtitle,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = 1f - transitionProgress }
                                .applyColorGrade(activeGradeName)
                        )
                        RenderClipVisual(
                            clipStyle = nextClip.clipStyle,
                            title = nextClip.title,
                            subtitle = nextClip.subtitle,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = transitionProgress }
                                .applyColorGrade(activeGradeName)
                        )
                    }
                    "Zoom Dynamic" -> {
                        // Outgoing slides scale outward, incoming zooms inward
                        RenderClipVisual(
                            clipStyle = activeClip.clipStyle,
                            title = activeClip.title,
                            subtitle = activeClip.subtitle,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = 1f - transitionProgress
                                    scaleX = 1f + (transitionProgress * 0.4f)
                                    scaleY = 1f + (transitionProgress * 0.4f)
                                }
                                .applyColorGrade(activeGradeName)
                        )
                        RenderClipVisual(
                            clipStyle = nextClip.clipStyle,
                            title = nextClip.title,
                            subtitle = nextClip.subtitle,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = transitionProgress
                                    scaleX = 0.7f + (transitionProgress * 0.3f)
                                    scaleY = 0.7f + (transitionProgress * 0.3f)
                                }
                                .applyColorGrade(activeGradeName)
                        )
                    }
                    "Glitch Blur" -> {
                        // Rapid horizontal pixel offsets mapping raw signal glitch look
                        val glitchJitterX = if (transitionProgress in 0.1f..0.8f) {
                            sin(transitionProgress * 110f) * 45f
                        } else 0f
                        RenderClipVisual(
                            clipStyle = activeClip.clipStyle,
                            title = activeClip.title,
                            subtitle = activeClip.subtitle,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = 1f - transitionProgress
                                    translationX = glitchJitterX
                                    rotationZ = glitchJitterX * 0.05f
                                }
                                .applyColorGrade(activeGradeName)
                        )
                        RenderClipVisual(
                            clipStyle = nextClip.clipStyle,
                            title = nextClip.title,
                            subtitle = nextClip.subtitle,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = transitionProgress
                                    translationX = -glitchJitterX
                                }
                                .applyColorGrade(activeGradeName)
                        )
                    }
                    "Wipe Right" -> {
                        // Sliding page-push wipe
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { translationX = transitionProgress * size.width }
                                .applyColorGrade(activeGradeName)
                        ) {
                            RenderClipVisual(
                                clipStyle = activeClip.clipStyle,
                                title = activeClip.title,
                                subtitle = activeClip.subtitle
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { translationX = (transitionProgress - 1f) * size.width }
                                .applyColorGrade(activeGradeName)
                        ) {
                            RenderClipVisual(
                                clipStyle = nextClip.clipStyle,
                                title = nextClip.title,
                                subtitle = nextClip.subtitle
                            )
                        }
                    }
                    else -> {
                        // Basic abrupt transition
                        val activeStyle = if (transitionProgress < 0.5f) activeClip else nextClip
                        RenderClipVisual(
                            clipStyle = activeStyle.clipStyle,
                            title = activeStyle.title,
                            subtitle = activeStyle.subtitle,
                            modifier = Modifier
                                .fillMaxSize()
                                .applyColorGrade(activeGradeName)
                        )
                    }
                }
            }
        }
    }
}

// Media slider and timeline controller
@Composable
fun PlaybackControlBar(viewModel: VideoEditorViewModel) {
    val totalMs = viewModel.totalDurationMs
    val currentMs = viewModel.currentTimeMs

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.togglePlayback() },
                    modifier = Modifier.background(SurfaceDark, CircleShape)
                ) {
                    Icon(
                        imageVector = if (viewModel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Playback",
                        tint = if (viewModel.isPlaying) Pink80 else PrimaryPurple
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${formatTime(currentMs)} / ${formatTime(totalMs)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ACTIVE GRADE: ${viewModel.activeProject?.selectedFilter}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Linear Timeline Slider
        Slider(
            value = if (totalMs > 0) currentMs.toFloat() / totalMs else 0f,
            onValueChange = { viewModel.seekTo((it * totalMs).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = PrimaryPurple,
                activeTrackColor = PrimaryPurple,
                inactiveTrackColor = OutlineGrey
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Desktop style track layout displaying multiple slides in sequence
@Composable
fun TimelineTrackRow(
    clips: List<ClipEntity>,
    currentTimeMs: Long,
    onSeekTo: (Long) -> Unit,
    onSelectSelect: (ClipEntity) -> Unit,
    onDeleteClip: (Int) -> Unit,
    onAddClip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .border(BorderStroke(1.dp, OutlineGrey))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STUDIO TIMELINE TRACKS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "${clips.size} Scenes",
                fontSize = 10.sp,
                color = PrimaryPurple
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var elapsedMs = 0L
                clips.forEachIndexed { index, clip ->
                    val clipStartMs = elapsedMs
                    val isCursorInside = currentTimeMs >= clipStartMs && currentTimeMs < (clipStartMs + clip.durationMs)

                    // Single clip card block
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isCursorInside) 2.dp else 1.dp,
                                color = if (isCursorInside) PrimaryPurple else OutlineGrey,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(SurfaceDark)
                            .clickable {
                                onSeekTo(clipStartMs)
                                onSelectSelect(clip)
                            }
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0${index + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                                Text(
                                    text = "${clip.durationMs / 1000}s",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = clip.title.ifEmpty { "Scene $index" },
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = clip.clipStyle.replace("_", " "),
                                color = TextSecondary,
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Delete item key
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ ${clip.transitionType}",
                                    fontSize = 7.sp,
                                    color = PrimaryPurple
                                )
                                IconButton(
                                    onClick = { onDeleteClip(index) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    // Draw transition indicator between blocks
                    if (index < clips.size - 1) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceDark)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 10.sp,
                                color = PrimaryPurple
                            )
                        }
                    }

                    elapsedMs += clip.durationMs
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Append custom clip Button
                OutlinedButton(
                    onClick = { onAddClip() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.5f)),
                    modifier = Modifier.height(72.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Add Slide", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Interactive Tabs Content
@Composable
fun TabHeaderButton(title: String, isActive: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isActive) PrimaryPurple else TextSecondary
        ),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.drawBehind {
            if (isActive) {
                // Glow neon active tab bar indicators
                drawRect(
                    color = PrimaryPurple,
                    topLeft = Offset(0f, size.height - 3.dp.toPx()),
                    size = Size(size.width, 3.dp.toPx())
                )
            }
        }
    ) {
        Text(title, fontSize = 12.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium)
    }
}

// AI Copilot module utilizing standard REST request
@Composable
fun AiAssistantTab(viewModel: VideoEditorViewModel) {
    val context = LocalContext.current
    Column {
        Text(
            text = "AI CHOREOGRAPHER COPILOT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Describe your presentation goal or social media highlight, and Google Gemini Flash will build the entire slides roadmap, coordinate animations, and apply color filters.",
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        OutlinedTextField(
            value = viewModel.aiPromptInput,
            onValueChange = { viewModel.aiPromptInput = it },
            placeholder = { Text("Describe details, styles, objectives...", color = TextSecondary.copy(alpha = 0.6f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = OutlineGrey,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (viewModel.isAnalyzingWithAi) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryPurple, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = viewModel.aiStatusMessage,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (com.example.BuildConfig.GEMINI_API_KEY.isEmpty() || com.example.BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY") {
                            Toast.makeText(context, "API Key not found inside secrets!", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.choreographWithAi()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple,
                        contentColor = OnPrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = OnPrimaryPurple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate with Gemini", color = OnPrimaryPurple, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Color grades selection module
@Composable
fun ColorGradeTab(viewModel: VideoEditorViewModel, activeProject: ProjectEntity) {
    Column {
        Text(
            text = "AUTOMATIC COLOR GRADING STYLE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Select an automatic look-up-table (LUT) matrix to apply instant professional grading to your presentation cards.",
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorGrading.presets.chunked(2).forEach { pair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { preset ->
                        val isSelected = activeProject.selectedFilter.lowercase() == preset.name.lowercase()
                        Card(
                            onClick = { viewModel.updateColorGrade(preset.name) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) SurfaceDark else BackgroundDark
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) PrimaryPurple else OutlineGrey
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = preset.name,
                                    color = if (isSelected) PrimaryPurple else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = preset.description,
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Transition editing panel
@Composable
fun TransitionsTab(viewModel: VideoEditorViewModel) {
    val clips = viewModel.currentClips.value
    Column {
        Text(
            text = "CHOREOGRAPH SLIDE TRANSITIONS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Setup transition actions between timeline scenes. Select a slide track from the timeline above and double click to modify transition timing directly.",
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        if (clips.all { it.transitionType == "None" }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .border(1.dp, OutlineGrey, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Transitions set to [None]. Press any timeline slide card to add transitions.", color = TextSecondary, fontSize = 11.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                clips.forEachIndexed { idx, clip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                            .border(1.dp, OutlineGrey, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(BackgroundDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                  Text("0${idx + 1}", fontSize = 10.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(clip.title.ifEmpty { "Scene $idx" }, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Style: ${clip.clipStyle}", color = TextSecondary, fontSize = 9.sp)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryPurple.copy(alpha = 0.1f))
                                .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ ${clip.transitionType}",
                                fontSize = 10.sp,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Project settings panel
@Composable
fun SetupProjectTab(viewModel: VideoEditorViewModel, allProjects: List<ProjectEntity>) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Column {
        Text(
            text = "STUDIO PROJECT WORKSPACE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Switch active editing project or delete the current workspace completely.",
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // New creation field inline
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Quick Project Title", color = TextSecondary.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = OutlineGrey,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (title.trim().isNotEmpty()) {
                            viewModel.createNewProject(title, desc)
                            title = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple,
                        contentColor = OnPrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add", color = OnPrimaryPurple, fontWeight = FontWeight.Bold)
                }
            }

            if (allProjects.isNotEmpty()) {
                Text("Library Projects:", fontSize = 11.sp, color = TextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allProjects.forEach { proj ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                                .border(1.dp, OutlineGrey, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(proj.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(proj.description, color = TextSecondary, fontSize = 9.sp)
                            }
                            Button(
                                onClick = { viewModel.selectProject(proj) },
                                colors = ButtonDefaults.buttonColors(containerColor = BackgroundDark),
                                border = BorderStroke(1.dp, OutlineGrey),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Switch", fontSize = 10.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = OutlineGrey)
            Button(
                onClick = { viewModel.deleteCurrentProject() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete Active Workspace", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Overlay dialog to customize active timeline card metadata
@Composable
fun ClipEditorOverlay(viewModel: VideoEditorViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, OutlineGrey, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EDIT SLIDE SCENE CONFIG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
                IconButton(onClick = { viewModel.clearEditSelection() }) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = viewModel.editClipTitle,
                onValueChange = { viewModel.editClipTitle = it },
                label = { Text("Slide Headline") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = OutlineGrey,
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.editClipSubtitles,
                onValueChange = { viewModel.editClipSubtitles = it },
                label = { Text("Bullet features list (Split with | characters)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = OutlineGrey,
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Selectors row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Style selection
                Box(modifier = Modifier.weight(1f)) {
                    var expandedStyle by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { expandedStyle = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, OutlineGrey),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = viewModel.editClipStyle.replace("_", " "),
                            fontSize = 11.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    DropdownMenu(
                        expanded = expandedStyle,
                        onDismissRequest = { expandedStyle = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        listOf(
                            "CORPORATE_SLIDE", "TEAM_PHOTO", "METRICS_CHART",
                            "SOCIAL_PRODUCT", "CAFE_HIGHLIGHT", "SUNSET_SUMMARY"
                        ).forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.replace("_", " "), color = TextPrimary, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.editClipStyle = st
                                    expandedStyle = false
                                }
                            )
                        }
                    }
                }

                // Transition selection
                Box(modifier = Modifier.weight(1f)) {
                    var expandedTrans by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { expandedTrans = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, OutlineGrey),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ ${viewModel.editClipTransition}",
                            fontSize = 11.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    DropdownMenu(
                        expanded = expandedTrans,
                        onDismissRequest = { expandedTrans = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        listOf("Cross Fade", "Zoom Dynamic", "Glitch Blur", "Wipe Right", "None").forEach { tr ->
                            DropdownMenuItem(
                                text = { Text(tr, color = TextPrimary, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.editClipTransition = tr
                                    expandedTrans = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.saveActiveEdit() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    contentColor = OnPrimaryPurple
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Commit Config Changes", fontWeight = FontWeight.Bold, color = OnPrimaryPurple)
            }
        }
    }
}

// Rendering dialog overlay
@Composable
fun ExportingProgressOverlay(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, OutlineGrey),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .width(280.dp)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                CircularProgressIndicator(
                    color = PrimaryPurple,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "COMPILING PRESENTATION",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Blending transitions & grading layers...",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                LinearProgressIndicator(
                    progress = { progress },
                    color = PrimaryPurple,
                    trackColor = OutlineGrey,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(progress * 100).toInt()}% Rendered",
                    fontSize = 11.sp,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Success Dialogue
@Composable
fun ExportSuccessDialog(
    fileName: String,
    uri: Uri,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Slide Complete", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Your polished project '$fileName' has been rendered successfully and saved to your device Downloads directory!",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDark, RoundedCornerShape(12.dp))
                        .border(1.dp, OutlineGrey, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "File Name: ${uri.lastPathSegment ?: "pitch_presentation_export.html"}\nPath: Downloads/",
                        fontSize = 9.sp,
                        color = PrimaryPurple,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    contentColor = OnPrimaryPurple
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", color = OnPrimaryPurple, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceDark
    )
}

// Time formatter MM:SS.f
private fun formatTime(ms: Long): String {
    val sec = ms / 1000
    val min = sec / 60
    val remSec = sec % 60
    val remMs = (ms % 1000) / 100
    return String.format("%02d:%02d.%d", min, remSec, remMs)
}
