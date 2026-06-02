package com.example.ui.style

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RenderClipVisual(
    clipStyle: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
    ) {
        when (clipStyle.uppercase()) {
            "CORPORATE_SLIDE" -> DrawCorporateSlide(title, subtitle)
            "TEAM_PHOTO" -> DrawTeamPhoto(title, subtitle)
            "METRICS_CHART" -> DrawMetricsChart(title, subtitle)
            "SOCIAL_PRODUCT" -> DrawSocialProduct(title, subtitle)
            "CAFE_HIGHLIGHT" -> DrawCafeHighlight(title, subtitle)
            "SUNSET_SUMMARY" -> DrawSunsetSummary(title, subtitle)
            else -> DrawCorporateSlide(title, subtitle) // Default fallback
        }
    }
}

@Composable
private fun DrawCorporateSlide(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
            .padding(24.dp)
    ) {
        // Tech Grid behind the slide
        Canvas(modifier = Modifier.fillMaxSize()) {
            val columns = 8
            val rows = 6
            val colStep = size.width / columns
            val rowStep = size.height / rows
            for (i in 1..columns) {
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(i * colStep, 0f),
                    end = Offset(i * colStep, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (i in 1..rows) {
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(0f, i * rowStep),
                    end = Offset(size.width, i * rowStep),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CORPORATE KEYNOTE",
                        fontSize = 9.sp,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title.ifEmpty { "Corporate Strategy" },
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Bullet points list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                val bullets = if (subtitle.isNotEmpty()) subtitle.split("|") else listOf(
                    "Accelerating digital workflows securely",
                    "Seamless transitions & automation tools",
                    "Driving strategic quarterly metrics"
                )
                bullets.forEach { bullet ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bullet.trim(),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "CONFIDENTIAL",
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Slide 01",
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun DrawTeamPhoto(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF2C3E50), Color(0xFF000000))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TEAM COLLABORATION",
                fontSize = 9.sp,
                color = Color(0xFFFFD54F),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            // Dynamic Custom Avatar drawings
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(vertical = 12.dp)
            ) {
                val centerWidth = size.width / 2
                val avatarY = size.height / 2 + 10f
                val radius = 30.dp.toPx()

                // Draw 3 overlapping team avatars with cute modern look
                val avatarOffsets = listOf(
                    Offset(centerWidth - 70.dp.toPx(), avatarY), // Left
                    Offset(centerWidth + 70.dp.toPx(), avatarY), // Right
                    Offset(centerWidth, avatarY - 8.dp.toPx()) // Center (Foreground)
                )

                val avatarColors = listOf(
                    Color(0xFF4FC3F7), // Blue
                    Color(0xFFFF8A65), // Orange
                    Color(0xFF81C784)  // Green
                )

                avatarOffsets.forEachIndexed { i, offset ->
                    // Draw neck/shoulders path
                    val shoulderPath = Path().apply {
                        moveTo(offset.x - radius * 1.3f, offset.y + radius * 1.5f)
                        quadraticBezierTo(
                            offset.x, offset.y + radius * 0.8f,
                            offset.x + radius * 1.3f, offset.y + radius * 1.5f
                        )
                        close()
                    }
                    drawPath(shoulderPath, color = avatarColors[i].copy(alpha = 0.8f))

                    // Draw head circle
                    drawCircle(
                        color = avatarColors[i],
                        radius = radius,
                        center = offset
                    )

                    // Draw simple stylized glasses / hair / accessories
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = radius * 0.2f,
                        center = Offset(offset.x - radius * 0.3f, offset.y)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = radius * 0.2f,
                        center = Offset(offset.x + radius * 0.3f, offset.y)
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(offset.x - radius * 0.1f, offset.y),
                        end = Offset(offset.x + radius * 0.1f, offset.y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title.ifEmpty { "MEET THE TEAM" },
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle.ifEmpty { "Engineers & Creative Designers coordinate seamlessly" },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun DrawMetricsChart(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PERFORMANCE OVERVIEW",
                        fontSize = 8.sp,
                        color = Color(0xFFFF4081),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title.ifEmpty { "Revenue & Scaling Graph" },
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4081).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFFFF4081),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Draw Real Canvas bar graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                val gridAlpha = 0.08f
                val gridYCount = 4
                val yStep = size.height / gridYCount
                // Grid lines
                for (i in 0..gridYCount) {
                    drawLine(
                        color = Color.White,
                        alpha = gridAlpha,
                        start = Offset(0f, i * yStep),
                        end = Offset(size.width, i * yStep),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Render stylized bars with top values
                val barCount = 5
                val spacerRatio = 0.4f
                val barWidth = size.width / (barCount + (barCount + 1)*spacerRatio)
                val spacerWidth = barWidth * spacerRatio

                val heights = listOf(0.4f, 0.65f, 0.55f, 0.85f, 0.95f)
                val gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF4081), Color(0xFF9C27B0))
                )

                for (idx in 0 until barCount) {
                    val x = spacerWidth + idx * (barWidth + spacerWidth)
                    val h = size.height * heights[idx]
                    val y = size.height - h

                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Glow line on top
                    drawLine(
                        color = Color(0xFFFFB74D),
                        start = Offset(x, y),
                        end = Offset(x + barWidth, y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = subtitle.ifEmpty { "Q1 Core Metrics • UP 145%" },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "LATEST TERM",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun DrawSocialProduct(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SOCIAL FEATURE SPOTLIGHT",
                fontSize = 8.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            // Draw a phone mockup with products
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(95.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    // Title inside screen
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White)
                        )
                    }

                    // Product Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.3f))
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // A star illustration inside
                                    drawCircle(Color(0xFFFFD54F), radius = size.minDimension / 4)
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(5.dp)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(25.dp)
                                        .height(4.dp)
                                        .background(Color.White.copy(alpha = 0.6f))
                                )
                            }
                        }
                    }

                    // Bottom buy bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "$49.00", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFFFD54F))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "BUY NOW", fontSize = 7.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title.ifEmpty { "Premium Coffee Maker" },
                    fontSize = 15.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle.ifEmpty { "Elegant matte finish, dual spout." },
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DrawCafeHighlight(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3E2723)) // Rich Coffee brown
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAILY SPECIALS",
                fontSize = 8.sp,
                color = Color(0xFFFFCC80),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            // Warm Vector mug drawn on canvas
            Canvas(
                modifier = Modifier
                    .size(60.dp)
            ) {
                val circleCenter = Offset(size.width / 2, size.height / 2)
                val r = size.minDimension / 3

                // Plate ellipse
                drawOval(
                    color = Color(0xFFFFCC80).copy(alpha = 0.2f),
                    topLeft = Offset(size.width / 2 - r * 1.5f, size.height / 2 + r * 0.7f),
                    size = Size(r * 3f, r * 0.6f)
                )

                // Handle path
                val handlePath = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            circleCenter.x + r * 0.4f, circleCenter.y - r * 0.5f,
                            circleCenter.x + r * 1.4f, circleCenter.y + r * 0.5f
                        )
                    )
                }
                drawPath(
                    path = handlePath,
                    color = Color(0xFFFFCC80),
                    style = Stroke(width = 4.dp.toPx())
                )

                // Mug body
                drawRoundRect(
                    color = Color(0xFFFFCC80),
                    topLeft = Offset(circleCenter.x - r, circleCenter.y - r),
                    size = Size(r * 2f, r * 1.8f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Steam lines
                val steamStartX = circleCenter.x - 10f
                val steamStartY = circleCenter.y - r - 8f
                drawPath(
                    path = Path().apply {
                        moveTo(steamStartX, steamStartY)
                        quadraticBezierTo(steamStartX - 5f, steamStartY - 10f, steamStartX, steamStartY - 20f)
                        moveTo(steamStartX + 10f, steamStartY)
                        quadraticBezierTo(steamStartX + 15f, steamStartY - 10f, steamStartX + 10f, steamStartY - 20f)
                    },
                    color = Color.White.copy(alpha = 0.5f),
                    style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title.ifEmpty { "Muted Espresso Blend" },
                    fontSize = 16.sp,
                    color = Color(0xFFFFCC80),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle.ifEmpty { "Artisanal espresso with velvety oat milk" },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun DrawSunsetSummary(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE65100), Color(0xFFFF8F00), Color(0xFFFFC107))
                )
            )
            .padding(24.dp)
    ) {
        // Landscape mountains silhouette
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width

            // Draw orange glow sun
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = 35.dp.toPx(),
                center = Offset(w * 0.7f, h * 0.45f)
            )

            // Mountain back path
            val mountainBack = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.7f)
                lineTo(w * 0.35f, h * 0.55f)
                lineTo(w * 0.6f, h * 0.72f)
                lineTo(w * 0.8f, h * 0.62f)
                lineTo(w, h * 0.75f)
                lineTo(w, h)
                close()
            }
            drawPath(mountainBack, color = Color(0xFF6D4C41).copy(alpha = 0.8f))

            // Mountain front path
            val mountainFront = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.8f)
                lineTo(w * 0.45f, h * 0.68f)
                lineTo(w * 0.75f, h * 0.82f)
                lineTo(w, h * 0.72f)
                lineTo(w, h)
                close()
            }
            drawPath(mountainFront, color = Color(0xFF3E2723))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SUMMER MEMORY HIGHLIGHT",
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Column {
                Text(
                    text = title.ifEmpty { "Sunset Beach Cruise" },
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle.ifEmpty { "Capturing golden hour in Miami Highlights 2026." },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontFamily = FontFamily.Serif
                )
            }
        }
    }
}
