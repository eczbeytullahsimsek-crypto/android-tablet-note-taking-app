package studio.murekkep.input

import android.view.MotionEvent

/**
 * Palm Rejection (Avuç İçi Reddi)
 *
 * Android tablet kullanıcıları kalemi ekrana bastırırken avuç içi
 * doğal olarak ekrana değer. Bu sınıf:
 * 1. TOOL_TYPE_STYLUS gördüğü an Stylus-Priority moduna geçer.
 * 2. Avuç içi geniş temas alanını (TOUCH_MAJOR) algılar ve yok sayar.
 * 3. Kalem etkinken parmak dokunuşlarını çizimden düşürüp yalnızca
 *    iki parmaklı pinch-zoom / pan hareketine yönlendirir.
 */
class PalmRejectionFilter(
    var isEnabled: Boolean = true
) {
    private var isStylusActive: Boolean = false

    fun processEvent(event: MotionEvent): Decision {
        if (!isEnabled) {
            return Decision(acceptForDrawing = true, isPalm = false, isStylus = false)
        }

        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val toolType = event.getToolType(pointerIndex)
        val isStylus = toolType == MotionEvent.TOOL_TYPE_STYLUS ||
                       toolType == MotionEvent.TOOL_TYPE_ERASER

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (isStylus) isStylusActive = true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                if (isStylus && event.pointerCount <= 1) isStylusActive = false
            }
        }

        // Avuç içi tespiti: Geniş temas alanı + düşük basınç
        val touchMajor = try {
            event.getHistoricalAxisValue(MotionEvent.AXIS_TOUCH_MAJOR, 0)
        } catch (_: Exception) {
            event.touchMajor
        }
        val isPalmContact = touchMajor > PALM_TOUCH_MAJOR_THRESHOLD_DP &&
                            event.pressure < PALM_PRESSURE_THRESHOLD

        if (isPalmContact) {
            return Decision(acceptForDrawing = false, isPalm = true, isStylus = isStylus)
        }

        // Kalem aktifken parmak dokunuşları çizime izin vermez (sadece pinch/pan)
        val acceptDrawing = if (isStylusActive) {
            isStylus
        } else {
            // Stylus yoksa parmakla çizime izin ver, ancak çoklu dokunmada çizimi kes
            event.pointerCount == 1 && !isPalmContact
        }

        return Decision(
            acceptForDrawing = acceptDrawing,
            isPalm = false,
            isStylus = isStylus
        )
    }

    data class Decision(
        val acceptForDrawing: Boolean,
        val isPalm: Boolean,
        val isStylus: Boolean
    )

    companion object {
        private const val PALM_TOUCH_MAJOR_THRESHOLD_DP = 85f
        private const val PALM_PRESSURE_THRESHOLD = 0.20f
    }
}
