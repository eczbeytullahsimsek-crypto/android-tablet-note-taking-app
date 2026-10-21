package studio.murekkep.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.murekkep.model.InkStroke
import studio.murekkep.model.StrokePoint
import studio.murekkep.model.ToolType

/**
 * Android Tablet Compose Arayüzü:
 * - Parmakla Pinch-to-Zoom & Pan (Ölçek: %25 - %400)
 * - Palm Rejection Durumu
 * - Kalem / Vurgulayıcı / Silgi Araç Çubuğu
 * - Kağıt Zemini Çizgileri
 */
@Composable
fun NotebookScreen() {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var palmRejectionEnabled by remember { mutableStateOf(true) }
    var selectedTool by remember { mutableStateOf(ToolType.PEN) }
    var strokeColor by remember { mutableStateOf(Color(0xFF16213A)) }

    val strokes = remember { mutableStateListOf<InkStroke>() }
    var currentStrokePoints by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Üst Bar
        Surface(
            color = Color(0xFFE6DDCE),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MÜREKKEP · ANDROID TABLET",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6B6257),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Lineer Cebir — Ders 4",
                        fontSize = 18.sp,
                        color = Color(0xFF16213A)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Zoom Göstergesi
                    Text(
                        text = "%${(scale * 100).toInt()}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF16213A),
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    // Palm Rejection Butonu
                    FilterChip(
                        selected = palmRejectionEnabled,
                        onClick = { palmRejectionEnabled = !palmRejectionEnabled },
                        label = {
                            Text(
                                if (palmRejectionEnabled) "Avuç Reddi: AÇIK"
                                else "Avuç Reddi: KAPALI"
                            )
                        }
                    )
                }
            }
        }

        // Çizim & Yakınlaştırma Tuvali
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFE4DCCC))
                // Parmakla Pinch-Zoom & Pan (Google Kalem / Gesture)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.25f, 4.0f)
                        offset += pan
                    }
                }
        ) {
            // Kağıt Sayfası
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFFCFAF5),
                shadowElevation = 8.dp
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Çizgili Kağıt Zemin Deseni
                    val stepY = 38.dp.toPx()
                    var y = stepY * 2
                    while (y < size.height) {
                        drawLine(
                            color = Color(0xFF2C3E63).copy(alpha = 0.12f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += stepY
                    }
                    // Kırmızı Sol Kenar Boşluk Çizgisi
                    drawLine(
                        color = Color(0xFFC0432B).copy(alpha = 0.25f),
                        start = Offset(86.dp.toPx(), 0f),
                        end = Offset(86.dp.toPx(), size.height),
                        strokeWidth = 1.5f
                    )

                    // Kaydedilmiş Vuruşlar
                    strokes.forEach { stroke ->
                        if (stroke.points.size > 1) {
                            val path = Path().apply {
                                moveTo(stroke.points.first().x, stroke.points.first().y)
                                stroke.points.drop(1).forEach { pt ->
                                    lineTo(pt.x, pt.y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color(stroke.color),
                                style = Stroke(
                                    width = stroke.width,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
