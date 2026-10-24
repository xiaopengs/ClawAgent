package com.qoder.clawagent.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.qoder.clawagent.model.AttachmentKind
import com.qoder.clawagent.model.ChatAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale

data class ImportedFileInfo(
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val sizeLabel: String,
    val previewText: String?,
)

/**
 * Reads a picked image into a bounded JPEG data URL. The snapshot is stored on
 * the message so retry and engine switching do not need the original URI grant.
 */
suspend fun loadImageAttachment(context: Context, uri: Uri): ChatAttachment = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val meta = readMetadata(context, uri)
    val mime = resolver.getType(uri) ?: "image/jpeg"
    val original = resolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
        ?: throw IllegalArgumentException("无法读取这张图片")
    val scaled = downscale(original, maxSide = 1280)
    val bytes = ByteArrayOutputStream().use { output ->
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, output)
        output.toByteArray()
    }
    if (original !== scaled) original.recycle()
    scaled.recycle()
    ChatAttachment(
        id = "image-${System.currentTimeMillis()}-${uri.hashCode()}",
        kind = AttachmentKind.IMAGE,
        name = meta.first.ifBlank { "图片.jpg" },
        mimeType = "image/jpeg",
        sizeBytes = bytes.size.toLong(),
        dataUrl = "data:image/jpeg;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}",
    )
}

/** Reads text files up to a bounded limit and keeps metadata only for binaries. */
suspend fun loadFileAttachment(context: Context, uri: Uri): ChatAttachment = withContext(Dispatchers.IO) {
    val information = inspectFile(context, uri)
    ChatAttachment(
        id = "file-${System.currentTimeMillis()}-${uri.hashCode()}",
        kind = AttachmentKind.FILE,
        name = information.name,
        mimeType = information.mimeType,
        sizeBytes = information.sizeBytes,
        text = information.previewText,
    )
}

suspend fun inspectFile(context: Context, uri: Uri): ImportedFileInfo = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val (name, size) = readMetadata(context, uri)
    val mime = resolver.getType(uri).orEmpty().ifBlank { guessMime(name) }
    val text = if (isTextLike(name, mime)) {
        runCatching {
            resolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(16_384)
                val output = ByteArrayOutputStream()
                var total = 0
                while (total < MAX_TEXT_BYTES) {
                    val read = input.read(buffer, 0, minOf(buffer.size, MAX_TEXT_BYTES - total))
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                    total += read
                }
                String(output.toByteArray(), Charsets.UTF_8)
            }
        }.getOrNull()
    } else {
        null
    }
    ImportedFileInfo(
        name = name.ifBlank { "未命名文件" },
        mimeType = mime,
        sizeBytes = size,
        sizeLabel = formatSize(size),
        previewText = text,
    )
}

fun formatSize(bytes: Long): String = when {
    bytes <= 0 -> "未知大小"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024f)
    else -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
}

fun decodeAttachmentBitmap(dataUrl: String?): ImageBitmap? {
    if (dataUrl.isNullOrBlank()) return null
    return runCatching {
        val payload = dataUrl.substringAfter("base64,", missingDelimiterValue = "")
        if (payload.isBlank()) return null
        val bytes = Base64.decode(payload, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
}

private fun readMetadata(context: Context, uri: Uri): Pair<String, Long> {
    var name = ""
    var size = 0L
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) name = cursor.getString(nameIndex).orEmpty()
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        }
    }
    return name.ifBlank { uri.lastPathSegment?.substringAfterLast('/').orEmpty() } to size
}

private fun downscale(source: Bitmap, maxSide: Int): Bitmap {
    val longest = maxOf(source.width, source.height)
    if (longest <= maxSide) return source
    val ratio = maxSide.toFloat() / longest
    return Bitmap.createScaledBitmap(
        source,
        (source.width * ratio).toInt().coerceAtLeast(1),
        (source.height * ratio).toInt().coerceAtLeast(1),
        true,
    )
}

private fun isTextLike(name: String, mime: String): Boolean {
    if (mime.startsWith("text/")) return true
    if (mime in TEXT_MIME_TYPES) return true
    val extension = name.substringAfterLast('.', "").lowercase(Locale.US)
    return extension in TEXT_EXTENSIONS
}

private fun guessMime(name: String): String {
    val extension = name.substringAfterLast('.', "").lowercase(Locale.US)
    return when (extension) {
        "md" -> "text/markdown"
        "txt", "log" -> "text/plain"
        "json" -> "application/json"
        "csv" -> "text/csv"
        "pdf" -> "application/pdf"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }
}

private const val MAX_TEXT_BYTES = 16_384

private val TEXT_EXTENSIONS = setOf(
    "txt", "md", "json", "csv", "tsv", "xml", "yaml", "yml", "log",
    "kt", "kts", "java", "js", "ts", "tsx", "jsx", "py", "go", "rs",
    "html", "css", "sql", "sh", "toml", "ini",
)

private val TEXT_MIME_TYPES = setOf(
    "application/json",
    "application/xml",
    "application/javascript",
    "application/x-yaml",
    "application/csv",
)
