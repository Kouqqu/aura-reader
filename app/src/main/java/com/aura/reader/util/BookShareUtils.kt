package com.aura.reader.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object BookShareUtils {

    fun shareBookFile(context: Context, book: Book) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sharedDir = File(context.cacheDir, "shared_books").apply { mkdirs() }
                val safeTitle = book.title.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().ifBlank { "book" }
                val ext = book.format.name.lowercase()
                val shareFile = File(sharedDir, "$safeTitle.$ext")

                var resolved = false

                // 1. Check direct file:// URI
                if (book.uriString.startsWith("file://") || !book.uriString.contains("://")) {
                    val path = if (book.uriString.startsWith("file://")) {
                        Uri.parse(book.uriString).path ?: book.uriString.removePrefix("file://")
                    } else {
                        book.uriString
                    }
                    val file = File(path)
                    if (file.exists() && file.canRead() && file.length() > 0) {
                        file.copyTo(shareFile, overwrite = true)
                        resolved = true
                    }
                }

                // 2. Check saved_books internal directory
                if (!resolved) {
                    val savedDir = File(context.filesDir, "saved_books")
                    if (savedDir.exists()) {
                        val candidates = savedDir.listFiles() ?: emptyArray()
                        val match = candidates.find {
                            it.name.startsWith(book.id) || (it.length() > 0 && it.name.contains(book.id.take(8)))
                        }
                        if (match != null && match.exists() && match.length() > 0) {
                            match.copyTo(shareFile, overwrite = true)
                            resolved = true
                        }
                    }
                }

                // 3. Fallback: try opening InputStream via contentResolver
                if (!resolved) {
                    try {
                        val uri = Uri.parse(book.uriString)
                        val stream = context.contentResolver.openInputStream(uri)
                        if (stream != null) {
                            stream.use { input ->
                                shareFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            if (shareFile.exists() && shareFile.length() > 0) {
                                resolved = true
                            }
                        }
                    } catch (e: Exception) {}
                }

                if (!resolved || !shareFile.exists() || shareFile.length() == 0L) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Не удалось прочитать файл книги для отправки", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    shareFile
                )

                val mimeType = when (book.format) {
                    BookFormat.FB2 -> "application/x-fictionbook+xml"
                    BookFormat.EPUB -> "application/epub+zip"
                    BookFormat.PDF -> "application/pdf"
                    BookFormat.TXT -> "text/plain"
                    BookFormat.MOBI -> "application/x-mobipocket-ebook"
                    BookFormat.CBZ -> "application/vnd.comicbook+zip"
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_SUBJECT, book.title)
                    putExtra(Intent.EXTRA_TEXT, "${book.title} — ${book.author}".trim(' ', '—', ' '))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, book.title).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                withContext(Dispatchers.Main) {
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка при отправке книги: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
