package com.aura.reader.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.aura.reader.MainActivity
import com.aura.reader.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TtsVoiceInfo(
    val name: String,
    val locale: String,
    val displayName: String
)

data class TtsState(
    val isPlaying: Boolean = false,
    val isServiceRunning: Boolean = false,
    val currentParagraphIndex: Int = 0,
    val totalParagraphs: Int = 0,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val selectedVoiceName: String? = null,
    val availableVoices: List<TtsVoiceInfo> = emptyList(),
    val currentText: String = "",
    val bookTitle: String = "",
    val chapterTitle: String = "",
    val isInitialized: Boolean = false
)

class BookTtsService : Service(), TextToSpeech.OnInitListener {

    companion object {
        const val CHANNEL_ID = "aura_tts_channel"
        const val NOTIFICATION_ID = 1002

        const val ACTION_START = "com.aura.reader.tts.START"
        const val ACTION_PLAY_PAUSE = "com.aura.reader.tts.PLAY_PAUSE"
        const val ACTION_PLAY = "com.aura.reader.tts.PLAY"
        const val ACTION_PAUSE = "com.aura.reader.tts.PAUSE"
        const val ACTION_PREV = "com.aura.reader.tts.PREV"
        const val ACTION_NEXT = "com.aura.reader.tts.NEXT"
        const val ACTION_STOP = "com.aura.reader.tts.STOP"
        const val ACTION_SET_SPEED = "com.aura.reader.tts.SET_SPEED"
        const val ACTION_SET_PITCH = "com.aura.reader.tts.SET_PITCH"
        const val ACTION_SET_VOICE = "com.aura.reader.tts.SET_VOICE"

        const val EXTRA_BOOK_TITLE = "extra_book_title"
        const val EXTRA_CHAPTER_TITLE = "extra_chapter_title"
        const val EXTRA_PARAGRAPHS = "extra_paragraphs"
        const val EXTRA_START_INDEX = "extra_start_index"
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_PITCH = "extra_pitch"
        const val EXTRA_VOICE_NAME = "extra_voice_name"

        private val _ttsState = MutableStateFlow(TtsState())
        val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

        fun start(
            context: Context,
            bookTitle: String,
            chapterTitle: String,
            paragraphs: ArrayList<String>,
            startIndex: Int = 0,
            speed: Float = 1.0f
        ) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_BOOK_TITLE, bookTitle)
                putExtra(EXTRA_CHAPTER_TITLE, chapterTitle)
                putStringArrayListExtra(EXTRA_PARAGRAPHS, paragraphs)
                putExtra(EXTRA_START_INDEX, startIndex)
                putExtra(EXTRA_SPEED, speed)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun playPause(context: Context) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_PLAY_PAUSE
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                context.startService(intent)
            }
        }

        fun next(context: Context) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_NEXT
            }
            context.startService(intent)
        }

        fun prev(context: Context) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_PREV
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun setSpeed(context: Context, speed: Float) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_SET_SPEED
                putExtra(EXTRA_SPEED, speed)
            }
            context.startService(intent)
        }

        fun setPitch(context: Context, pitch: Float) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_SET_PITCH
                putExtra(EXTRA_PITCH, pitch)
            }
            context.startService(intent)
        }

        fun setVoice(context: Context, voiceName: String) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_SET_VOICE
                putExtra(EXTRA_VOICE_NAME, voiceName)
            }
            context.startService(intent)
        }
    }

    private var tts: TextToSpeech? = null
    private var mediaSession: MediaSession? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private var paragraphs: List<String> = emptyList()
    private var currentIndex: Int = 0
    private var bookTitle: String = ""
    private var chapterTitle: String = ""
    private var speechRate: Float = 1.0f
    private var speechPitch: Float = 1.0f
    private var selectedVoice: String? = null
    private var isTtsReady: Boolean = false

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pausePlayback()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initMediaSession()
        tts = TextToSpeech(this, this)

        registerReceiver(
            noisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langResult = tts?.setLanguage(Locale.getDefault())
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to Russian or English
                val ruResult = tts?.setLanguage(Locale("ru", "RU"))
                if (ruResult == TextToSpeech.LANG_MISSING_DATA || ruResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
            }
            tts?.setSpeechRate(speechRate)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    if (_ttsState.value.isPlaying) {
                        nextParagraph()
                    }
                }

                override fun onError(utteranceId: String?) {
                    if (_ttsState.value.isPlaying) {
                        nextParagraph()
                    }
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    // Intentionally stopped/paused, do nothing
                }
            })
            val voicesList = try {
                tts?.voices?.map { v ->
                    TtsVoiceInfo(
                        name = v.name,
                        locale = v.locale.displayLanguage,
                        displayName = "${v.locale.displayLanguage} (${v.name.takeLast(10)})"
                    )
                }?.distinctBy { it.name }?.sortedBy { it.displayName } ?: emptyList()
            } catch (e: Throwable) {
                emptyList()
            }
            isTtsReady = true
            _ttsState.value = _ttsState.value.copy(
                isInitialized = true,
                availableVoices = voicesList,
                selectedVoiceName = tts?.voice?.name
            )

            // If we already had paragraphs pending, start speaking
            if (paragraphs.isNotEmpty() && _ttsState.value.isPlaying) {
                speakCurrentParagraph()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                bookTitle = intent.getStringExtra(EXTRA_BOOK_TITLE) ?: ""
                chapterTitle = intent.getStringExtra(EXTRA_CHAPTER_TITLE) ?: ""
                paragraphs = intent.getStringArrayListExtra(EXTRA_PARAGRAPHS) ?: emptyList()
                currentIndex = intent.getIntExtra(EXTRA_START_INDEX, 0).coerceIn(0, (paragraphs.size - 1).coerceAtLeast(0))
                speechRate = intent.getFloatExtra(EXTRA_SPEED, 1.0f)

                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
                if (selectedVoice != null) {
                    try {
                        val matched = tts?.voices?.firstOrNull { it.name == selectedVoice }
                        if (matched != null) tts?.voice = matched
                    } catch (e: Throwable) {}
                }

                _ttsState.value = _ttsState.value.copy(
                    isPlaying = true,
                    isServiceRunning = true,
                    currentParagraphIndex = currentIndex,
                    totalParagraphs = paragraphs.size,
                    speed = speechRate,
                    pitch = speechPitch,
                    selectedVoiceName = selectedVoice ?: tts?.voice?.name,
                    currentText = paragraphs.getOrNull(currentIndex) ?: "",
                    bookTitle = bookTitle,
                    chapterTitle = chapterTitle,
                    isInitialized = isTtsReady
                )

                requestAudioFocus()
                startForeground(NOTIFICATION_ID, buildNotification(isPlaying = true))
                updateMediaSessionState(isPlaying = true)

                if (isTtsReady) {
                    speakCurrentParagraph()
                }
            }
            ACTION_PLAY_PAUSE -> {
                if (_ttsState.value.isPlaying) {
                    pausePlayback()
                } else {
                    resumePlayback()
                }
            }
            ACTION_PLAY -> resumePlayback()
            ACTION_PAUSE -> pausePlayback()
            ACTION_NEXT -> nextParagraph()
            ACTION_PREV -> previousParagraph()
            ACTION_STOP -> stopPlayback()
            ACTION_SET_SPEED -> {
                val newSpeed = intent.getFloatExtra(EXTRA_SPEED, 1.0f)
                speechRate = newSpeed
                tts?.setSpeechRate(speechRate)
                _ttsState.value = _ttsState.value.copy(speed = speechRate)
                if (_ttsState.value.isPlaying) {
                    speakCurrentParagraph()
                }
            }
            ACTION_SET_PITCH -> {
                val newPitch = intent.getFloatExtra(EXTRA_PITCH, 1.0f)
                speechPitch = newPitch
                tts?.setPitch(speechPitch)
                _ttsState.value = _ttsState.value.copy(pitch = speechPitch)
                if (_ttsState.value.isPlaying) {
                    speakCurrentParagraph()
                }
            }
            ACTION_SET_VOICE -> {
                val voiceName = intent.getStringExtra(EXTRA_VOICE_NAME)
                if (!voiceName.isNullOrBlank()) {
                    try {
                        val matched = tts?.voices?.firstOrNull { it.name == voiceName }
                        if (matched != null) {
                            tts?.voice = matched
                            selectedVoice = voiceName
                            _ttsState.value = _ttsState.value.copy(selectedVoiceName = voiceName)
                            if (_ttsState.value.isPlaying) {
                                speakCurrentParagraph()
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun speakCurrentParagraph() {
        if (!isTtsReady || paragraphs.isEmpty()) return
        val text = paragraphs.getOrNull(currentIndex)?.trim() ?: return
        if (text.isBlank()) {
            nextParagraph()
            return
        }

        val params = Bundle()
        val utteranceId = "aura_utterance_$currentIndex"
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

        _ttsState.value = _ttsState.value.copy(
            isPlaying = true,
            currentParagraphIndex = currentIndex,
            currentText = text
        )

        val notification = buildNotification(isPlaying = true)
        startForeground(NOTIFICATION_ID, notification)
        updateMediaSessionState(isPlaying = true)
    }

    private fun pausePlayback() {
        tts?.stop()
        abandonAudioFocus()
        _ttsState.value = _ttsState.value.copy(isPlaying = false)

        val notification = buildNotification(isPlaying = false)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
        updateMediaSessionState(isPlaying = false)
    }

    private fun resumePlayback() {
        requestAudioFocus()
        _ttsState.value = _ttsState.value.copy(isPlaying = true)
        speakCurrentParagraph()
    }

    private fun nextParagraph() {
        if (currentIndex < paragraphs.size - 1) {
            currentIndex++
            speakCurrentParagraph()
        } else {
            // Finished chapter
            pausePlayback()
        }
    }

    private fun previousParagraph() {
        if (currentIndex > 0) {
            currentIndex--
            speakCurrentParagraph()
        } else {
            speakCurrentParagraph()
        }
    }

    private fun stopPlayback() {
        tts?.stop()
        abandonAudioFocus()
        _ttsState.value = TtsState(isPlaying = false, isServiceRunning = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(this, "AuraReaderTts").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() { resumePlayback() }
                override fun onPause() { pausePlayback() }
                override fun onSkipToNext() { nextParagraph() }
                override fun onSkipToPrevious() { previousParagraph() }
                override fun onStop() { stopPlayback() }
            })
            isActive = true
        }
    }

    private fun updateMediaSessionState(isPlaying: Boolean) {
        val state = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val actions = PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_STOP

        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setActions(actions)
                .setState(state, currentIndex.toLong(), speechRate)
                .build()
        )
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, BookTtsService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, BookTtsService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, BookTtsService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, BookTtsService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val currentSnippet = paragraphs.getOrNull(currentIndex)?.take(120) ?: ""

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Пауза" else "Слушать"

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(bookTitle.ifBlank { "Aura Reader" })
            .setContentText(currentSnippet.ifBlank { chapterTitle })
            .setSubText(chapterTitle)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_previous, "Назад", prevIntent).build())
            .addAction(Notification.Action.Builder(playPauseIcon, playPauseTitle, playPauseIntent).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_next, "Вперед", nextIntent).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "Стоп", stopIntent).build())
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aura Reader Озвучка",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Управление воспроизведением озвучки книг"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest == null) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_LOSS -> pausePlayback()
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pausePlayback()
                            AudioManager.AUDIOFOCUS_GAIN -> if (_ttsState.value.isPlaying) resumePlayback()
                        }
                    }
                    .build()
            }

            audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        pausePlayback()
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(noisyReceiver)
        } catch (e: Exception) {}

        mediaSession?.release()
        tts?.stop()
        tts?.shutdown()
        abandonAudioFocus()
        _ttsState.value = TtsState()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
