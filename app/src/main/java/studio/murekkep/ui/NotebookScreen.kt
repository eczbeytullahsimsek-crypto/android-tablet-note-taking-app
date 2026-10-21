package studio.murekkep.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.murekkep.model.InkStroke
import studio.murekkep.model.StrokePoint
import studio.murekkep.model.ToolType

@Composable
fun NotebookScreen() {
    var scale by remember { mutableFloatStateOf(1f) }
    var palmRejectionEnabled by remember { mutableStateOf(true) }
    var selectedTool by remember { mutableStateOf(ToolType.PEN) }
    var strokeColor by remember { mutableStateOf(Color(0xFF16213A)) }
    val strokes = remember { mutableStateListOf<InkStroke>() }
    var currentStrokePoints by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = Color(0xFFE6DDCE),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("MÜREKKEP · ANDROID TABLET", fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace, color = Color(0xFF6B6257), letterSpacing = 2.sp)
                    Text("Lineer Cebir — Ders 4", fontSize = 18.sp, color = Color(0xFF16213A))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("%${(scale * 100).toInt()}", fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp, color = Color(0xFF16213A), modifier = Modifier.padding(end = 12.dp))
                    FilterChip(
                        selected = palmRejectionEnabled,
                        onClick = { palmRejectionEnabled = !palmRejectionEnabled },
                        label = { Text(if (palmRejectionEnabled) "Avuç Reddi: AÇIK" else "Avuç Reddi: KAPALI") }
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFE4DCCC))) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFFCFAF5),
                shadowElevation = 8.dp
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(selectedTool, strokeColor) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (selectedTool == ToolType.HAND) return@awaitEachGesture

                            val points = mutableListOf(
                                StrokePoint(down.position.x, down.position.y, 0.5f, System.currentTimeMillis())
                            )
                            currentStrokePoints = points.toList()
                            var finished = false
                            while (!finished) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change == null) {
                                    finished = true
                                } else if (change.pressed) {
                                    points.add(StrokePoint(
                                        change.position.x, change.position.y,
                                        change.pressure.coerceIn(0f, 1f), System.currentTimeMillis()
                                    ))
                                    currentStrokePoints = points.toList()
                                } else {
                                    finished = true
                                }
                            }

                            val color = when (selectedTool) {
                                ToolType.ERASER -> Color(0xFFFCFAF5)
                                ToolType.HIGHLIGHTER -> strokeColor.copy(alpha = 0.35f)
                                else -> strokeColor
                            }
                            val width = when (selectedTool) {
                                ToolType.ERASER -> 24.dp.toPx()
                                ToolType.HIGHLIGHTER -> 14.dp.toPx()
                                else -> 2.5.dp.toPx()
                            }
                            if (points.isNotEmpty()) {
                                strokes.add(InkStroke(
                                    tool = selectedTool,
                                    color = color.toArgb().toLong() and 0xFFFFFFFFL,
                                    width = width,
                                    points = points.toList()
                                ))
                            }
                            currentStrokePoints = emptyList()
                        }
                    }
                ) {
                    val stepY = 38.dp.toPx()
                    var y = stepY * 2
                    while (y < size.height) {
                        drawLine(Color(0xFF2C3E63).copy(alpha = 0.12f),
                            Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += stepY
                    }
                    drawLine(Color(0xFFC0432B).copy(alpha = 0.25f),
                        Offset(86.dp.toPx(), 0f), Offset(86.dp.toPx(), size.height), strokeWidth = 1.5f)

                    fun drawStroke(points: List<StrokePoint>, color: Color, width: Float) {
                        if (points.isEmpty()) return
                        if (points.size == 1) {
                            drawCircle(color, radius = width / 2f, center = Offset(points[0].x, points[0].y))
                        } else {
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                points.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }

                    strokes.forEach { stroke ->
                        drawStroke(stroke.points, Color(stroke.color.toInt()), stroke.width)
                    }
                    drawStroke(currentStrokePoints, strokeColor, 2.5.dp.toPx())
                }
            }
        }
    }
}
