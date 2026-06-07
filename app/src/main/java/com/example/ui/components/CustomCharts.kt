package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AttendanceCircleChart(
    rate: Float, // e.g. 0.95 for 95%
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(rate) {
        animateProgress.animateTo(rate, animationSpec = tween(1200))
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            // Background Circle
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // Foreground Active Arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animateProgress.value * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(rate * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Presensi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AcademicProgressLineChart(
    scores: List<Double>, // e.g., listOf(75.5, 82.0, 85.0, 92.5)
    labels: List<String>, // e.g., listOf("Agust", "Sept", "Okt", "Nov")
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    gradientColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall
    val animateFactor = remember { Animatable(0f) }

    LaunchedEffect(scores) {
        animateFactor.animateTo(1f, animationSpec = tween(1500))
    }

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (scores.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val paddingLeft = 40.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            val paddingTop = 12.dp.toPx()
            val paddingRight = 12.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingBottom - paddingTop

            // Draw Y-axis guide lines (0, 50, 100)
            val yLines = listOf(0.1f, 0.5f, 0.9f)
            val gridColor = lineColor.copy(alpha = 0.12f)
            for (lineRatio in yLines) {
                val y = paddingTop + chartHeight * lineRatio
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx()
                )
                // Draw Y labels (e.g. 90, 50, 10)
                val scoreVal = ((1f - lineRatio) * 100).toInt()
                drawText(
                    textMeasurer = textMeasurer,
                    text = scoreVal.toString(),
                    style = labelStyle.copy(fontSize = 10.sp, color = Color.Gray),
                    topLeft = Offset(8.dp.toPx(), y - 8.dp.toPx())
                )
            }

            // Calculate coordinates for points
            val points = mutableListOf<Offset>()
            val maxScore = 100.0
            val minScore = 0.0
            val scoreRange = maxScore - minScore

            val stepX = if (scores.size > 1) chartWidth / (scores.size - 1) else chartWidth

            for (i in scores.indices) {
                val score = scores[i]
                val x = paddingLeft + i * stepX
                val normalizedScore = (score - minScore) / scoreRange
                val y = (paddingTop + chartHeight - (normalizedScore * chartHeight)).toFloat()
                // Apply animation factor
                val animatedY = (paddingTop + chartHeight - ((normalizedScore * chartHeight) * animateFactor.value)).toFloat()
                points.add(Offset(x, animatedY))
            }

            // Draw Gradient Area Under the Path
            if (points.size > 1) {
                val fillPath = Path().apply {
                    moveTo(points[0].x, paddingTop + chartHeight)
                    for (point in points) {
                        lineTo(point.x, point.y)
                    }
                    lineTo(points.last().x, paddingTop + chartHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )

                // Draw Core Path line
                val strokePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw Points and labels
            for (i in points.indices) {
                val point = points[i]
                // Draw point circle
                drawCircle(
                    color = lineColor,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = point
                )

                // Draw score value text on top of points
                val scoreText = scores[i].toInt().toString()
                drawText(
                    textMeasurer = textMeasurer,
                    text = scoreText,
                    style = labelStyle.copy(fontSize = 11.sp, color = lineColor),
                    topLeft = Offset(point.x - 8.dp.toPx(), point.y - 20.dp.toPx())
                )

                // Draw X-axis label
                if (i < labels.size) {
                    val label = labels[i]
                    drawText(
                        textMeasurer = textMeasurer,
                        text = label,
                        style = labelStyle.copy(fontSize = 10.sp, color = Color.Gray),
                        topLeft = Offset(point.x - 12.dp.toPx(), paddingTop + chartHeight + 4.dp.toPx())
                    )
                }
            }
        }
    }
}
