package com.aura.reader.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import com.aura.reader.data.service.CoverSearchService
import com.aura.reader.ui.theme.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CoverDownloadUtils {

    fun saveCoverToGallery(
        context: Context,
        title: String,
        coverBase64: String?,
        coverUrl: String?,
        strings: Strings? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var bitmap: Bitmap? = null

                if (!coverBase64.isNullOrBlank()) {
                    val bytes = Base64.decode(coverBase64, Base64.DEFAULT)
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } else if (!coverUrl.isNullOrBlank()) {
                    val downloadedBase64 = CoverSearchService.downloadCoverAsBase64(coverUrl)
                    if (!downloadedBase64.isNullOrBlank()) {
                        val bytes = Base64.decode(downloadedBase64, Base64.DEFAULT)
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                }

                if (bitmap == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, strings?.coverSaveError ?: "Не удалось сохранить обложку", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val cleanTitle = title.replace(Regex("""[\\/:*?"<>|]"""), "_").trim().take(40).ifBlank { "book" }
                val fileName = "Cover_${cleanTitle}_${System.currentTimeMillis()}.jpg"

                var saved = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AuraReader")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }

                    val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                        }
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        context.contentResolver.update(uri, contentValues, null, null)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val auraDir = File(picturesDir, "AuraReader").apply { mkdirs() }
                    val imageFile = File(auraDir, fileName)
                    FileOutputStream(imageFile).use { out ->
                        saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    MediaScannerConnection.scanFile(context, arrayOf(imageFile.absolutePath), arrayOf("image/jpeg"), null)
                }

                withContext(Dispatchers.Main) {
                    if (saved) {
                        Toast.makeText(context, strings?.coverSavedToGallery ?: "Обложка сохранена в Галерею", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, strings?.coverSaveError ?: "Не удалось сохранить обложку", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, strings?.coverSaveError ?: "Ошибка при сохранении обложки", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
