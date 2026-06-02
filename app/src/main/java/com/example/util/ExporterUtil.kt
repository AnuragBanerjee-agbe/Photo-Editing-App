package com.example.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.entity.ClipEntity
import com.example.data.entity.ProjectEntity
import java.io.File
import java.io.FileOutputStream

object ExporterUtil {

    // Exports timeline to a modern, fully animated HTML5 client-ready presentation deck in real Downloads directory
    fun exportProjectToHtml(context: Context, project: ProjectEntity, clips: List<ClipEntity>): Uri? {
        val safeName = project.name.trim().lowercase().replace("[^a-z0-9]".toRegex(), "_")
        val fileName = if (safeName.isNotEmpty()) "${safeName}_ai_studio_export.html" else "pitch_presentation_export.html"
        val htmlContent = generateHtmlDeck(project, clips)

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/html")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os ->
                        os.write(htmlContent.toByteArray(Charsets.UTF_8))
                    }
                    uri
                } else {
                    // Fallback to internal storage if query failed
                    writeInternalFallbackFile(context, fileName, htmlContent)
                }
            } else {
                val extDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(extDownloads, fileName)
                FileOutputStream(file).use { os ->
                    os.write(htmlContent.toByteArray(Charsets.UTF_8))
                }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Ultimate fallback to app sandbox directory to prevent crash
            writeInternalFallbackFile(context, fileName, htmlContent)
        }
    }

    private fun writeInternalFallbackFile(context: Context, fileName: String, content: String): Uri? {
        return try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            FileOutputStream(file).use { os ->
                os.write(content.toByteArray(Charsets.UTF_8))
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generateHtmlDeck(project: ProjectEntity, clips: List<ClipEntity>): String {
        val gradingStyle = project.selectedFilter

        // Choose CSS filtering elements representing each auto color grade
        val cssFilter = when (gradingStyle.uppercase()) {
            "CORPORATE CLEAN" -> "contrast(1.08) brightness(1.04) saturate(1.02)"
            "WARM SUNSET" -> "sepia(0.25) saturate(1.22) contrast(1.1) hue-rotate(-5deg)"
            "CYBER TECH BLUE" -> "hue-rotate(20deg) saturate(1.15) contrast(1.1) brightness(0.9)"
            "RETRO CAM" -> "sepia(0.35) saturate(0.8) contrast(0.95)"
            "TEAL & ORANGE" -> "contrast(1.15) saturate(1.2) hue-rotate(5deg)"
            "B&W EDITORIAL" -> "grayscale(1) contrast(1.3) brightness(0.95)"
            else -> "none"
        }

        val slidesHtml = StringBuilder()
        clips.forEachIndexed { idx, clip ->
            val activeClass = if (idx == 0) "active" else ""
            val bullets = if (clip.subtitle.isNotEmpty()) clip.subtitle.split("|") else listOf(
                "Automated Presentation Flow",
                "Advanced Transitions Applied",
                "Built with Google Gemini AI"
            )

            // Select slide style background gradients mimicking our Canvas renderers
            val backgroundCss = when (clip.clipStyle.uppercase()) {
                "CORPORATE_SLIDE" -> "background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);"
                "TEAM_PHOTO" -> "background: radial-gradient(circle, #2c3e50, #000000);"
                "METRICS_CHART" -> "background: #0f0f1a; border: 1px solid rgba(255, 64, 129, 0.15);"
                "SOCIAL_PRODUCT" -> "background: linear-gradient(135deg, #6a11cb, #2575fc);"
                "CAFE_HIGHLIGHT" -> "background: #2D1A12;"
                "SUNSET_SUMMARY" -> "background: vertical-gradient, linear-gradient(180deg, #E65100, #FF8F00, #FFC107);"
                else -> "background: linear-gradient(135deg, #1e1e38, #303056);"
            }

            // Decide transition speed & style for presentation flow
            val transStyle = clip.transitionType.lowercase()
            val transDurationSec = (clip.transitionDurationMs / 1000f)

            slidesHtml.append("""
                <div class="slide $activeClass" data-index="$idx" data-transition="$transStyle" style="$backgroundCss filter: $cssFilter;">
                    <div class="watermark">${clip.clipStyle.replace("_", " ")}</div>
                    
                    <div class="slide-content">
                        <div class="badge">${project.selectedFilter.uppercase()}</div>
                        <h1 class="title">${clip.title.ifEmpty { "Strategy Board" }}</h1>
                        
                        ${if (clip.clipStyle.uppercase() == "METRICS_CHART") """
                        <div class="chart-container">
                            <div class="bar" style="height: 40%">40%</div>
                            <div class="bar" style="height: 65%">65%</div>
                            <div class="bar" style="height: 55%">55%</div>
                            <div class="bar animate" style="height: 85%">85%</div>
                            <div class="bar animate" style="height: 95%">95%</div>
                        </div>
                        """ else ""}
                        
                        ${if (clip.clipStyle.uppercase() == "TEAM_PHOTO") """
                        <div class="team-avatars">
                            <div class="avatar blue">👨‍💻</div>
                            <div class="avatar orange">👩‍🎨</div>
                            <div class="avatar green">🧑‍💼</div>
                        </div>
                        """ else ""}
                        
                        ${if (clip.clipStyle.uppercase() == "CAFE_HIGHLIGHT") """
                        <div class="mug-visual">☕</div>
                        """ else ""}

                        <ul class="bullets">
                            ${bullets.joinToString("") { "<li>" + it.trim() + "</li>" }}
                        </ul>
                    </div>
                    
                    <div class="footer">
                        <span>Project: ${project.name}</span>
                        <span>Slide ${idx + 1} of ${clips.size} (${clip.durationMs / 1000}s)</span>
                    </div>
                </div>
            """.trimIndent())
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${project.name} - AI Video Storyboard Deck</title>
            <style>
                * {
                    box-sizing: border-box;
                    margin: 0;
                    padding: 0;
                }
                body, html {
                    width: 100%;
                    height: 100%;
                    background-color: #0c0c14;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    overflow: hidden;
                    color: white;
                }
                .presentation-container {
                    position: relative;
                    width: 100%;
                    height: 100%;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                .slide {
                    position: absolute;
                    width: 100%;
                    height: 100%;
                    opacity: 0;
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                    padding: 60px;
                    transition: all 0.8s ease-in-out;
                    z-index: 1;
                    visibility: hidden;
                }
                .slide.active {
                    opacity: 1;
                    visibility: visible;
                    z-index: 2;
                }
                
                /* Transitions styles active */
                .slide.fade-in {
                    opacity: 0;
                }
                .slide.fade-in.active {
                    opacity: 1;
                }
                
                .slide.page-slide {
                    transform: translateX(100%);
                }
                .slide.page-slide.active {
                    transform: translateX(0);
                }
                
                .slide.zoom-dynamic {
                    transform: scale(0.6);
                }
                .slide.zoom-dynamic.active {
                    transform: scale(1);
                }
                
                .slide.glitch-blur {
                    filter: blur(20px) contrast(2) saturate(2);
                }
                .slide.glitch-blur.active {
                    filter: $cssFilter;
                }

                .slide-content {
                    max-width: 800px;
                    margin: auto 0;
                }
                .badge {
                    display: inline-block;
                    background: rgba(255, 255, 255, 0.15);
                    padding: 4px 10px;
                    font-size: 11px;
                    font-weight: bold;
                    letter-spacing: 1px;
                    border-radius: 4px;
                    margin-bottom: 20px;
                    text-transform: uppercase;
                }
                .title {
                    font-size: 48px;
                    line-height: 1.2;
                    margin-bottom: 24px;
                    font-weight: 800;
                    letter-spacing: -1px;
                }
                .bullets {
                    list-style-type: none;
                }
                .bullets li {
                    font-size: 20px;
                    margin-bottom: 12px;
                    opacity: 0.85;
                    display: flex;
                    align-items: center;
                }
                .bullets li::before {
                    content: "•";
                    color: cyan;
                    font-weight: bold;
                    display: inline-block;
                    width: 1.5em;
                    margin-left: -0.2em;
                }
                .watermark {
                    position: absolute;
                    top: 40px;
                    left: 60px;
                    font-size: 12px;
                    font-weight: bold;
                    letter-spacing: 2px;
                    color: rgba(255, 255, 255, 0.35);
                    text-transform: uppercase;
                }
                .footer {
                    display: flex;
                    justify-content: space-between;
                    font-size: 11px;
                    opacity: 0.5;
                    letter-spacing: 0.5px;
                }
                
                /* Extra Visual elements */
                .chart-container {
                    display: flex;
                    gap: 15px;
                    align-items: flex-end;
                    height: 180px;
                    margin: 30px 0;
                    padding-bottom: 5px;
                    border-bottom: 1px solid rgba(255,255,255,0.1);
                    width: 400px;
                }
                .bar {
                    flex: 1;
                    background: linear-gradient(180deg, #ff4081, #9c27b0);
                    border-radius: 4px 4px 0 0;
                    display: flex;
                    justify-content: center;
                    align-items: flex-end;
                    padding-bottom: 8px;
                    font-size: 11px;
                    font-weight: bold;
                    transition: height 1s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                }
                .team-avatars {
                    display: flex;
                    gap: 15px;
                    margin: 25px 0;
                }
                .avatar {
                    width: 50px;
                    height: 50px;
                    border-radius: 50%;
                    background: rgba(255,255,255,0.15);
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    font-size: 24px;
                    box-shadow: 0 4px 10px rgba(0,0,0,0.3);
                }
                .mug-visual {
                    font-size: 64px;
                    margin: 20px 0;
                }
                
                /* Controls bar */
                .navbar-controls {
                    position: absolute;
                    bottom: 30px;
                    right: 40px;
                    display: flex;
                    gap: 10px;
                    z-index: 100;
                }
                .btn {
                    background: rgba(0,0,0,0.6);
                    border: 1px solid rgba(255,255,255,0.2);
                    padding: 10px 16px;
                    border-radius: 20px;
                    color: white;
                    cursor: pointer;
                    font-size: 13px;
                    font-weight: bold;
                    transition: background 0.2s, transform 0.1s;
                }
                .btn:hover {
                    background: rgba(255,255,255,0.15);
                    transform: scale(1.05);
                }
                .btn:active {
                    transform: scale(0.95);
                }
                .meta-tags {
                    position: absolute;
                    top: 40px;
                    right: 60px;
                    font-size: 11px;
                    background: rgba(0,0,0,0.4);
                    padding: 4px 12px;
                    border-radius: 12px;
                    border: 1px solid rgba(255,255,255,0.15);
                    color: aqua;
                }
            </style>
        </head>
        <body>
            <div class="presentation-container">
                <div class="meta-tags">AI Active Grade: ${gradingStyle}</div>
                
                $slidesHtml

                <div class="navbar-controls">
                    <button class="btn" onclick="prevSlide()">◀ PREV</button>
                    <button class="btn" onclick="autoPlay()">🍿 AUTOPLAY</button>
                    <button class="btn" onclick="nextSlide()">NEXT ▶</button>
                </div>
            </div>

            <script>
                let currentIdx = 0;
                const slides = document.querySelectorAll('.slide');
                const total = slides.length;
                let autoTimer = null;

                function showSlide(index) {
                    slides.forEach(s => {
                        s.classList.remove('active');
                        // Remove potential animation classes
                        s.style.transform = '';
                    });
                    
                    currentIdx = (index + total) % total;
                    const activeSlide = slides[currentIdx];
                    activeSlide.classList.add('active');
                }

                function nextSlide() {
                    showSlide(currentIdx + 1);
                }

                function prevSlide() {
                    showSlide(currentIdx - 1);
                }

                function autoPlay() {
                    if (autoTimer) {
                        clearInterval(autoTimer);
                        autoTimer = null;
                        document.querySelector('button[onclick="autoPlay()"]').innerText = "🍿 AUTOPLAY";
                    } else {
                        document.querySelector('button[onclick="autoPlay()"]').innerText = "⏸ PAUSE";
                        autoTimer = setInterval(() => {
                            nextSlide();
                        }, 4000);
                    }
                }

                // Keyboard controls
                document.addEventListener('keydown', (e) => {
                    if (e.key === 'ArrowRight' || e.key === 'Space') {
                        nextSlide();
                    } else if (e.key === 'ArrowLeft') {
                        prevSlide();
                    }
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}
