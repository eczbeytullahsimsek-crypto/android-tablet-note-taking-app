package studio.murekkep.ink

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import kotlinx.coroutines.tasks.await
import studio.murekkep.model.InkStroke

/**
 * Google Kalem Motoru (ML Kit Digital Ink Recognition)
 *
 * El yazısı vuruşlarını Google'ın makine öğrenmesi modeli üzerinden
 * Türkçe ("tr") ve çok dilli metne dönüştürür.
 */
class GoogleInkEngine(
    private val context: Context,
    private val languageTag: String = "tr"
) {

    private val modelIdentifier: DigitalInkRecognitionModelIdentifier? =
        DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)

    private val model: DigitalInkRecognitionModel? =
        modelIdentifier?.let { DigitalInkRecognitionModel.builder(it).build() }

    private val recognizer: DigitalInkRecognizer? =
        model?.let {
            DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(it).build()
            )
        }

    /**
     * Dil modelini indirir (cihazda yoksa WiFi üzerinden çeker).
     */
    suspend fun ensureModelDownloaded(): Boolean {
        val m = model ?: return false
        val manager = RemoteModelManager.getInstance()
        val isDownloaded = manager.isModelDownloaded(m).await()
        if (isDownloaded) return true

        return try {
            val conditions = DownloadConditions.Builder().build()
            manager.download(m, conditions).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Çizilen InkStroke listesini ML Kit Ink nesnesine dönüştürür.
     */
    fun buildMlKitInk(strokes: List<InkStroke>): Ink {
        val inkBuilder = Ink.builder()
        strokes.forEach { stroke ->
            val strokeBuilder = Ink.Stroke.builder()
            stroke.points.forEach { pt ->
                strokeBuilder.addPoint(
                    Ink.Point.create(pt.x, pt.y, pt.timestamp)
                )
            }
            inkBuilder.addStroke(strokeBuilder.build())
        }
        return inkBuilder.build()
    }

    /**
     * Vuruşları metne dönüştürür (Google Kalem Motoru).
     */
    suspend fun recognizeText(strokes: List<InkStroke>): List<String> {
        val rec = recognizer ?: return emptyList()
        val ink = buildMlKitInk(strokes)
        return try {
            val result = rec.recognize(ink).await()
            result.candidates.map { it.text }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun close() {
        recognizer?.close()
    }
}
