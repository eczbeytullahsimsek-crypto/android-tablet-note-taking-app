package studio.murekkep.model

data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 0.5f,
    val timestamp: Long = System.currentTimeMillis()
)

data class InkStroke(
    val tool: ToolType = ToolType.PEN,
    val color: Long = 0xFF16213AL,
    val width: Float = 4f,
    val points: List<StrokePoint> = emptyList()
)

enum class ToolType {
    PEN,
    HIGHLIGHTER,
    ERASER,
    HAND
}

enum class PaperBackground {
    RULED,
    GRID,
    DOT,
    BLANK,
    PDF
}

data class NotebookPage(
    val id: Long,
    val label: String,
    val background: PaperBackground = PaperBackground.RULED,
    val strokes: List<InkStroke> = emptyList(),
    val pdfPageIndex: Int? = null
)

data class Folder(
    val id: Long,
    val name: String,
    val color: Long,
    val documentCount: Int = 0
)

data class Document(
    val id: Long,
    val folderId: Long?,
    val title: String,
    val subtitle: String,
    val isPdf: Boolean = false,
    val isStarred: Boolean = false,
    val pages: List<NotebookPage> = emptyList()
)
