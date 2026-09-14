package com.aura.reader.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.Base64
import android.view.View
import android.widget.RemoteViews
import com.aura.reader.MainActivity
import com.aura.reader.R
import com.aura.reader.data.model.Book
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class BookAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAllWidgets(context)
    }

    companion object {
        fun updateAllWidgets(context: Context, book: Book? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BookAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (appWidgetIds.isEmpty()) return

            if (book != null) {
                for (widgetId in appWidgetIds) {
                    val views = buildRemoteViews(context, book)
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } else {
                // Read the last opened book from SharedPreferences / DataStore recent books
                CoroutineScope(Dispatchers.IO).launch {
                    val lastBook = loadLastBook(context)
                    for (widgetId in appWidgetIds) {
                        val views = buildRemoteViews(context, lastBook)
                        appWidgetManager.updateAppWidget(widgetId, views)
                    }
                }
            }
        }

        private fun buildRemoteViews(context: Context, book: Book?): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_current_book)

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            if (book == null) {
                views.setViewVisibility(R.id.widget_book_content, View.GONE)
                views.setViewVisibility(R.id.widget_empty_content, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_book_content, View.VISIBLE)
                views.setViewVisibility(R.id.widget_empty_content, View.GONE)

                views.setTextViewText(R.id.widget_book_title, book.title)
                views.setTextViewText(R.id.widget_book_author, book.author.ifBlank { "Aura Reader" })
                views.setTextViewText(R.id.widget_progress_text, "${book.progressPercent}%")
                views.setProgressBar(R.id.widget_progress_bar, 100, book.progressPercent, false)

                // Render rounded cover bitmap from Base64 if available
                val coverBmp = decodeRoundedCover(book.coverBase64)
                if (coverBmp != null) {
                    views.setImageViewBitmap(R.id.widget_book_cover, coverBmp)
                } else {
                    views.setImageViewResource(R.id.widget_book_cover, R.mipmap.ic_launcher)
                }
            }

            return views
        }

        private fun decodeRoundedCover(base64Str: String?): Bitmap? {
            if (base64Str.isNullOrBlank()) return null
            return try {
                val bytes = Base64.decode(base64Str, Base64.DEFAULT)
                val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
                getRoundedCornerBitmap(original, 20f)
            } catch (e: Exception) {
                null
            }
        }

        private fun getRoundedCornerBitmap(bitmap: Bitmap, cornerRadiusPx: Float): Bitmap {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)

            canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
            return output
        }

        private fun loadLastBook(context: Context): Book? {
            try {
                val prefs = context.getSharedPreferences("reader_preferences", Context.MODE_PRIVATE)
                val json = prefs.getString("recent_books_json", null) ?: return null
                val arr = JSONArray(json)
                if (arr.length() == 0) return null
                val o = arr.getJSONObject(0)

                return Book(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    author = o.optString("author", ""),
                    coverBase64 = o.optString("coverBase64", null),
                    format = com.aura.reader.data.model.BookFormat.valueOf(o.optString("format", "EPUB")),
                    uriString = o.optString("uriString", ""),
                    progressPercent = o.optInt("progressPercent", 0),
                    currentChapterIndex = o.optInt("currentChapterIndex", 0),
                    currentScrollOffset = o.optInt("currentScrollOffset", 0)
                )
            } catch (e: Exception) {
                return null
            }
        }
    }
}
