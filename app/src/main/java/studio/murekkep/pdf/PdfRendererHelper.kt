package studio.murekkep.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Android Yerel PdfRenderer Sarmalayıcısı
 *
 * PDF sayfalarını bitmap olarak rasterize eder;
 * kullanıcı bu bitmap üzerinde Google kalem motoru ile serbestçe çizim yapabilir.
 */
class PdfRendererHelper(private val context: Context) {

    private var fileDescriptor: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null

    suspend fun open(uri: Uri): Int = withContext(Dispatchers.IO) {
        close()
        val tempFile = File(context.cacheDir, "opened_doc_${System.currentTimeMillis()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(fileDescriptor!!)
        renderer?.pageCount ?: 0
    }

    suspend fun renderPage(pageIndex: Int, targetWidth: Int = 1200): Bitmap? =
        withContext(Dispatchers.IO) {
            val r = renderer ?: return@withContext null
            if (pageIndex < 0 || pageIndex >= r.pageCount) return@withContext null

            val page = r.openPage(pageIndex)
            val aspectRatio = page.height.toFloat() / page.width.toFloat()
            val targetHeight = (targetWidth * aspectRatio).toInt()

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap
        }

    fun getPageCount(): Int = renderer?.pageCount ?: 0

    fun close() {
        renderer?.close()
        fileDescriptor?.close()
        renderer = null
        fileDescriptor = null
    }
}
