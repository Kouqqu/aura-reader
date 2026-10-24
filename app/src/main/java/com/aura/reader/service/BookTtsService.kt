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
import android.media.AudioDeviceInfo
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class TtsVoiceInfo(
    val name: String,
    val locale: String,
    val displayName: String,
    val languageCode: String = "",
    val languageDisplayName: String = "",
    val isNetworkRequired: Boolean = false
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
    val isInitialized: Boolean = false,
    val currentWordStart: Int = -1,
    val currentWordEnd: Int = -1,
    val currentSpokenWord: String = "",
    val currentOriginalBlockIndex: Int = -1,
    val chapterFinishedEvent: Long = 0L,
    val highlightDelayMs: Int = -1
)

data class TtsHighlightEvent(
    val utteranceId: String,
    val start: Int,
    val end: Int,
    val word: String,
    val arrivalUptimeMs: Long
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
        const val ACTION_PREVIEW_VOICE = "com.aura.reader.tts.PREVIEW_VOICE"
        const val ACTION_SEEK_PARAGRAPH = "com.aura.reader.tts.SEEK_PARAGRAPH"
        const val ACTION_SET_HIGHLIGHT_DELAY = "com.aura.reader.tts.SET_HIGHLIGHT_DELAY"
        const val EXTRA_PARAGRAPH_INDEX = "extra_paragraph_index"
        const val EXTRA_HIGHLIGHT_DELAY = "extra_highlight_delay"

        const val EXTRA_BOOK_TITLE = "extra_book_title"
        const val EXTRA_CHAPTER_TITLE = "extra_chapter_title"
        const val EXTRA_PARAGRAPHS = "extra_paragraphs"
        const val EXTRA_START_INDEX = "extra_start_index"
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_PITCH = "extra_pitch"
        const val EXTRA_VOICE_NAME = "extra_voice_name"
        const val EXTRA_START_TEXT = "extra_start_text"
        const val EXTRA_START_CHAR_OFFSET = "extra_start_char_offset"
        const val EXTRA_ORIGINAL_BLOCK_INDICES = "extra_original_block_indices"

        private val _ttsState = MutableStateFlow(TtsState())
        val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

        fun start(
            context: Context,
            bookTitle: String,
            chapterTitle: String,
            paragraphs: ArrayList<String>,
            startIndex: Int = 0,
            speed: Float = 1.0f,
            pitch: Float = 1.0f,
            initialVoice: String? = null,
            startText: String? = null,
            startCharOffset: Int = 0,
            originalBlockIndices: ArrayList<Int>? = null
        ) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_BOOK_TITLE, bookTitle)
                putExtra(EXTRA_CHAPTER_TITLE, chapterTitle)
                putStringArrayListExtra(EXTRA_PARAGRAPHS, paragraphs)
                putExtra(EXTRA_START_INDEX, startIndex)
                putExtra(EXTRA_SPEED, speed)
                putExtra(EXTRA_PITCH, pitch)
                if (initialVoice != null) {
                    putExtra(EXTRA_VOICE_NAME, initialVoice)
                }
                if (startText != null) {
                    putExtra(EXTRA_START_TEXT, startText)
                }
                putExtra(EXTRA_START_CHAR_OFFSET, startCharOffset)
                if (originalBlockIndices != null) {
                    putIntegerArrayListExtra(EXTRA_ORIGINAL_BLOCK_INDICES, originalBlockIndices)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
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

        fun previewVoice(context: Context, voiceName: String) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_PREVIEW_VOICE
                putExtra(EXTRA_VOICE_NAME, voiceName)
            }
            context.startService(intent)
        }
        fun seekParagraph(context: Context, paragraphIndex: Int) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_SEEK_PARAGRAPH
                putExtra(EXTRA_PARAGRAPH_INDEX, paragraphIndex)
            }
            context.startService(intent)
        }

        fun setHighlightDelay(context: Context, delayMs: Int) {
            val intent = Intent(context, BookTtsService::class.java).apply {
                action = ACTION_SET_HIGHLIGHT_DELAY
                putExtra(EXTRA_HIGHLIGHT_DELAY, delayMs)
            }
            context.startService(intent)
        }
    }

    private var tts: TextToSpeech? = null
    private var mediaSession: MediaSession? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private var paragraphs: List<String> = emptyList()
    private var originalBlockIndices: ArrayList<Int> = ArrayList()
    private var currentIndex: Int = 0
    private var bookTitle: String = ""
    private var chapterTitle: String = ""
    private var speechRate: Float = 1.0f
    private var speechPitch: Float = 1.0f
    private var selectedVoice: String? = null
    private var customFirstText: String? = null
    private var customFirstCharOffset: Int = 0
    private var currentBlockCharOffset: Int = 0
    private val utteranceCounter = java.util.concurrent.atomic.AtomicLong(0)
    @Volatile
    private var currentActiveUtteranceId: String? = null
    private var isTtsReady: Boolean = false
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var userHighlightDelayMs: Int = -1
    private val highlightChannel = Channel<TtsHighlightEvent>(Channel.UNLIMITED)
    private var highlightConsumerJob: Job? = null

    private fun isBluetoothAudioConnected(): Boolean {
        val am = audioManager ?: (getSystemService(Context.AUDIO_SERVICE) as? AudioManager) ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                devices.any {
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
                        it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER
                    ))
                }
            } catch (_: Throwable) {
                @Suppress("DEPRECATION")
                am.isBluetoothA2dpOn
            }
        } else {
            @Suppress("DEPRECATION")
            am.isBluetoothA2dpOn
        }
    }

    private fun getEffectiveHighlightDelayMs(): Long {
        if (userHighlightDelayMs >= 0) {
            return (userHighlightDelayMs.toFloat() / speechRate.coerceIn(0.5f, 3.0f)).toLong()
        }
        val isBt = isBluetoothAudioConnected()
        // Increased base delays: BT has larger audio buffer, speaker is smaller but still needs ~350ms
        val baseDelay = if (isBt) 600f else 350f
        return (baseDelay / speechRate.coerceIn(0.5f, 3.0f)).toLong()
    }

    private fun startHighlightConsumer() {
        highlightConsumerJob?.cancel()
        highlightConsumerJob = serviceScope.launch(Dispatchers.Main) {
            var lastWordDisplayUptime = 0L
            for (event in highlightChannel) {
                if (event.utteranceId != currentActiveUtteranceId || !_ttsState.value.isPlaying) {
                    continue
                }

                val delayCompensation = getEffectiveHighlightDelayMs()
                val targetTime = event.arrivalUptimeMs + delayCompensation
                val minWordDurationMs = (80f / speechRate.coerceIn(0.5f, 3.0f)).toLong()
                val scheduledTime = maxOf(targetTime, lastWordDisplayUptime + minWordDurationMs)

                val now = android.os.SystemClock.uptimeMillis()
                // Drop stale events: if we're running >2x the expected delay behind schedule,
                // it means the channel backlog is consuming old callbacks that are already
                // "in the past" relative to audio — skip them to avoid a highlight burst
                val maxAcceptableAge = delayCompensation * 2 + 200L
                val eventAge = now - event.arrivalUptimeMs
                if (eventAge > maxAcceptableAge && highlightChannel.isEmpty) {
                    // Only drop if there's nothing newer waiting — otherwise just let it pass
                    continue
                }

                val waitMs = scheduledTime - now
                if (waitMs > 0) {
                    delay(waitMs)
                }

                if (event.utteranceId == currentActiveUtteranceId && _ttsState.value.isPlaying) {
                    _ttsState.value = _ttsState.value.copy(
                        currentWordStart = event.start,
                        currentWordEnd = event.end,
                        currentSpokenWord = event.word
                    )
                    lastWordDisplayUptime = android.os.SystemClock.uptimeMillis()
                }
            }
        }
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pausePlayback()
            }
        }
    }

    @Volatile
    private var pendingPreviewVoice: String? = null

    private fun findVoiceByName(name: String): android.speech.tts.Voice? {
        return try {
            tts?.voices?.firstOrNull { it.name == name }
        } catch (_: Throwable) {
            null
        }
    }

    private fun applyVoiceByName(name: String): Boolean {
        val matched = findVoiceByName(name) ?: return false
        return try {
            tts?.voice = matched
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun detectGoogleOrSystemGender(rawName: String, features: Set<String>?): String {
        if (features != null) {
            if (features.any { it.contains("female", ignoreCase = true) }) return "female"
            if (features.any { it.contains("male", ignoreCase = true) && !it.contains("female", ignoreCase = true) }) return "male"
        }
        val lower = rawName.lowercase(Locale.ROOT)
        // Known Google Russian voices
        if (lower.contains("dfc") || lower.contains("rua") || lower.contains("ruc")) return "female"
        if (lower.contains("rub") || lower.contains("rud")) return "male"

        // Known Google English voices
        if (lower.contains("sfg") || lower.contains("iom") || lower.contains("iol")) return "female"
        if (lower.contains("iob") || lower.contains("tpd") || lower.contains("tpc")) return "male"

        if (lower.contains("female")) return "female"
        if (lower.contains("male")) return "male"
        return ""
    }

    private fun formatFriendlyVoiceName(rawName: String, counter: Int, features: Set<String>?): String {
        val lower = rawName.lowercase(Locale.ROOT)
        val nameMap = mapOf(
            "aleksandr" to "Александр",
            "aleksandr-hq" to "Александр (HQ)",
            "elena" to "Елена",
            "anna" to "Анна",
            "artemiy" to "Артемий",
            "yuriy" to "Юрий",
            "victoria" to "Виктория",
            "irina" to "Ирина",
            "tatiana" to "Татьяна",
            "mikhail" to "Михаил",
            "evgeniy" to "Евгений",
            "evgeniy-rus" to "Евгений",
            "pavel" to "Павел",
            "vitaliy" to "Виталий"
        )
        // 3rd party recognized voice: keep pure name without gender suffix
        if (nameMap.containsKey(lower)) {
            return nameMap[lower]!!
        }

        // Custom named engine voice (e.g. Alan, Bick) - also keep pure name
        if (!rawName.contains("-x-") && !rawName.matches(Regex("^[a-z]{2,3}[-_][a-z]{2,3}.*", RegexOption.IGNORE_CASE))) {
            val clean = rawName.replace(Regex("[-_]"), " ").trim()
            return clean.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }

        // Standard machine voice (Google/Samsung): append Male/Female
        val prefix = when (Locale.getDefault().language.lowercase(Locale.ROOT)) {
            "ru", "uk", "be" -> "Голос"
            "pl" -> "Głos"
            "cs" -> "Hlas"
            else -> "Voice"
        }

        val gender = detectGoogleOrSystemGender(rawName, features)
        val genderLabel = when (gender) {
            "female" -> when (Locale.getDefault().language.lowercase(Locale.ROOT)) {
                "ru" -> " (Женский)"
                "uk" -> " (Жіночий)"
                "be" -> " (Жаночы)"
                "pl" -> " (Żeński)"
                "cs" -> " (Ženský)"
                else -> " (Female)"
            }
            "male" -> when (Locale.getDefault().language.lowercase(Locale.ROOT)) {
                "ru" -> " (Мужской)"
                "uk" -> " (Чоловічий)"
                "be" -> " (Мужчынскі)"
                "pl" -> " (Męski)"
                "cs" -> " (Mužský)"
                else -> " (Male)"
            }
            else -> ""
        }
        return "$prefix $counter$genderLabel"
    }

    private fun getSampleTextForVoice(localeTag: String): String {
        val lang = if (localeTag.isNotBlank()) localeTag.take(2).lowercase(Locale.ROOT) else Locale.getDefault().language
        return when (lang) {
            "ru" -> "Привет! Это пример звучания голоса в читалке Aura."
            "uk" -> "Привіт! Це приклад звучання голосу в читалці Aura."
            "be" -> "Прывітанне! Гэта прыклад гучання голасу ў чыталцы Aura."
            "pl" -> "Cześć! To jest próbka głosu w czytniku Aura."
            "cs" -> "Ahoj! Toto je ukázka hlasu ve čtečce Aura."
            else -> "Hello! This is a voice sample in Aura Reader."
        }
    }

    private fun speakPreview(voiceName: String) {
        try {
            val matched = tts?.voices?.firstOrNull { it.name == voiceName }
            if (matched != null) {
                tts?.voice = matched
                val sampleText = getSampleTextForVoice(matched.locale.toLanguageTag())
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utterance_preview")
                }
                tts?.speak(sampleText, TextToSpeech.QUEUE_FLUSH, params, "utterance_preview")
            }
        } catch (_: Throwable) {}
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

        serviceScope.launch {
            try {
                com.aura.reader.data.preferences.PreferencesManager(applicationContext).ttsHighlightDelayMs.collect { delay ->
                    userHighlightDelayMs = delay
                    _ttsState.value = _ttsState.value.copy(highlightDelayMs = delay)
                }
            } catch (_: Exception) {}
        }
        startHighlightConsumer()
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
                override fun onStart(utteranceId: String?) {
                    if (utteranceId != currentActiveUtteranceId) return
                    _ttsState.value = _ttsState.value.copy(
                        currentWordStart = -1,
                        currentWordEnd = -1,
                        currentSpokenWord = ""
                    )
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "utterance_preview") return
                    if (utteranceId != currentActiveUtteranceId) return
                    _ttsState.value = _ttsState.value.copy(
                        currentWordStart = -1,
                        currentWordEnd = -1,
                        currentSpokenWord = ""
                    )
                    if (_ttsState.value.isPlaying) {
                        serviceScope.launch(Dispatchers.Main) {
                            if (utteranceId == currentActiveUtteranceId) {
                                nextParagraph()
                            }
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == "utterance_preview") return
                    if (utteranceId != currentActiveUtteranceId) return
                    _ttsState.value = _ttsState.value.copy(
                        currentWordStart = -1,
                        currentWordEnd = -1,
                        currentSpokenWord = ""
                    )
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    if (utteranceId == "utterance_preview") return
                    if (utteranceId != currentActiveUtteranceId) return
                    _ttsState.value = _ttsState.value.copy(
                        currentWordStart = -1,
                        currentWordEnd = -1,
                        currentSpokenWord = ""
                    )
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    if (utteranceId == currentActiveUtteranceId) {
                        _ttsState.value = _ttsState.value.copy(
                            currentWordStart = -1,
                            currentWordEnd = -1,
                            currentSpokenWord = ""
                        )
                    }
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    if (utteranceId != null && utteranceId == currentActiveUtteranceId) {
                        val fullText = paragraphs.getOrNull(currentIndex) ?: _ttsState.value.currentText
                        val adjustedStart = start + currentBlockCharOffset
                        val adjustedEnd = end + currentBlockCharOffset
                        val word = if (adjustedStart in 0..fullText.length && adjustedEnd in adjustedStart..fullText.length) {
                            fullText.substring(adjustedStart, adjustedEnd).trim()
                        } else ""
                        highlightChannel.trySend(
                            TtsHighlightEvent(
                                utteranceId = utteranceId,
                                start = adjustedStart,
                                end = adjustedEnd,
                                word = word,
                                arrivalUptimeMs = android.os.SystemClock.uptimeMillis()
                            )
                        )
                    }
                }
            })
            val voicesList = try {
                val rawVoices = tts?.voices ?: emptySet()
                val currentLang = Locale.getDefault().language.lowercase(Locale.ROOT)
                val grouped = rawVoices.groupBy { it.locale.language.lowercase(Locale.ROOT) }

                val sortedLangCodes = grouped.keys.sortedWith { a, b ->
                    if (a == currentLang) -1
                    else if (b == currentLang) 1
                    else {
                        val nameA = Locale(a).getDisplayLanguage(Locale.getDefault())
                        val nameB = Locale(b).getDisplayLanguage(Locale.getDefault())
                        nameA.compareTo(nameB, ignoreCase = true)
                    }
                }

                val result = mutableListOf<TtsVoiceInfo>()
                for (langCode in sortedLangCodes) {
                    val voicesInLang = grouped[langCode] ?: continue
                    val langDisplay = Locale(langCode).getDisplayLanguage(Locale.getDefault())
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                    val sortedVoices = voicesInLang.sortedWith(compareBy({ it.isNetworkConnectionRequired }, { it.name }))
                    var voiceNum = 1
                    for (v in sortedVoices) {
                        val isNetwork = v.isNetworkConnectionRequired
                        val friendly = formatFriendlyVoiceName(v.name, voiceNum, v.features)
                        voiceNum++
                        result.add(
                            TtsVoiceInfo(
                                name = v.name,
                                locale = v.locale.toLanguageTag(),
                                displayName = friendly,
                                languageCode = langCode,
                                languageDisplayName = langDisplay,
                                isNetworkRequired = isNetwork
                            )
                        )
                    }
                }
                result
            } catch (e: Throwable) {
                emptyList()
            }
            isTtsReady = true
            if (selectedVoice != null) {
                applyVoiceByName(selectedVoice!!)
            }
            _ttsState.value = _ttsState.value.copy(
                isInitialized = true,
                availableVoices = voicesList,
                selectedVoiceName = selectedVoice ?: tts?.voice?.name
            )

            if (pendingPreviewVoice != null) {
                speakPreview(pendingPreviewVoice!!)
                pendingPreviewVoice = null
            } else if (paragraphs.isNotEmpty() && _ttsState.value.isPlaying) {
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
                originalBlockIndices = intent.getIntegerArrayListExtra(EXTRA_ORIGINAL_BLOCK_INDICES) ?: ArrayList()
                currentIndex = intent.getIntExtra(EXTRA_START_INDEX, 0).coerceIn(0, (paragraphs.size - 1).coerceAtLeast(0))
                customFirstText = intent.getStringExtra(EXTRA_START_TEXT)
                customFirstCharOffset = intent.getIntExtra(EXTRA_START_CHAR_OFFSET, 0)
                currentBlockCharOffset = customFirstCharOffset
                speechRate = intent.getFloatExtra(EXTRA_SPEED, speechRate)
                speechPitch = intent.getFloatExtra(EXTRA_PITCH, speechPitch)
                val initVoice = intent.getStringExtra(EXTRA_VOICE_NAME)
                if (!initVoice.isNullOrBlank()) {
                    selectedVoice = initVoice
                }

                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
                if (selectedVoice != null) {
                    applyVoiceByName(selectedVoice!!)
                }

                _ttsState.value = _ttsState.value.copy(
                    isPlaying = true,
                    isServiceRunning = true,
                    currentParagraphIndex = currentIndex,
                    currentOriginalBlockIndex = originalBlockIndices.getOrNull(currentIndex) ?: currentIndex,
                    totalParagraphs = paragraphs.size,
                    speed = speechRate,
                    pitch = speechPitch,
                    selectedVoiceName = selectedVoice ?: tts?.voice?.name,
                    currentText = paragraphs.getOrNull(currentIndex) ?: "",
                    bookTitle = bookTitle,
                    chapterTitle = chapterTitle,
                    isInitialized = isTtsReady,
                    currentWordStart = -1,
                    currentWordEnd = -1,
                    currentSpokenWord = ""
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
                serviceScope.launch {
                    try {
                        com.aura.reader.data.preferences.PreferencesManager(applicationContext).setTtsSpeed(newSpeed)
                    } catch (_: Exception) {}
                }
                if (_ttsState.value.isPlaying) {
                    speakCurrentParagraph()
                }
            }
            ACTION_SET_PITCH -> {
                val newPitch = intent.getFloatExtra(EXTRA_PITCH, 1.0f)
                speechPitch = newPitch
                tts?.setPitch(speechPitch)
                _ttsState.value = _ttsState.value.copy(pitch = speechPitch)
                serviceScope.launch {
                    try {
                        com.aura.reader.data.preferences.PreferencesManager(applicationContext).setTtsPitch(newPitch)
                    } catch (_: Exception) {}
                }
                if (_ttsState.value.isPlaying) {
                    speakCurrentParagraph()
                }
            }
            ACTION_SET_VOICE -> {
                val voiceName = intent.getStringExtra(EXTRA_VOICE_NAME)
                if (!voiceName.isNullOrBlank()) {
                    if (applyVoiceByName(voiceName)) {
                        selectedVoice = voiceName
                        _ttsState.value = _ttsState.value.copy(selectedVoiceName = voiceName)
                        serviceScope.launch {
                            try {
                                com.aura.reader.data.preferences.PreferencesManager(applicationContext).setTtsSelectedVoice(voiceName)
                            } catch (_: Exception) {}
                        }
                        if (_ttsState.value.isPlaying) {
                            speakCurrentParagraph()
                        }
                    }
                }
            }
            ACTION_PREVIEW_VOICE -> {
                val voiceName = intent.getStringExtra(EXTRA_VOICE_NAME) ?: return START_NOT_STICKY
                if (isTtsReady) {
                    speakPreview(voiceName)
                } else {
                    pendingPreviewVoice = voiceName
                }
            }
            ACTION_SEEK_PARAGRAPH -> {
                val targetIdx = intent.getIntExtra(EXTRA_PARAGRAPH_INDEX, 0)
                if (paragraphs.isNotEmpty()) {
                    currentBlockCharOffset = 0
                    drainHighlightChannel()
                    currentIndex = targetIdx.coerceIn(0, paragraphs.size - 1)
                    _ttsState.value = _ttsState.value.copy(
                        currentParagraphIndex = currentIndex,
                        currentOriginalBlockIndex = originalBlockIndices.getOrNull(currentIndex) ?: currentIndex,
                        currentText = paragraphs.getOrNull(currentIndex) ?: "",
                        currentWordStart = -1,
                        currentWordEnd = -1,
                        currentSpokenWord = ""
                    )
                    if (_ttsState.value.isPlaying && isTtsReady) {
                        speakCurrentParagraph()
                    }
                }
            }
            ACTION_SET_HIGHLIGHT_DELAY -> {
                val delayMs = intent.getIntExtra(EXTRA_HIGHLIGHT_DELAY, -1)
                userHighlightDelayMs = delayMs
                _ttsState.value = _ttsState.value.copy(highlightDelayMs = delayMs)
                serviceScope.launch {
                    try {
                        com.aura.reader.data.preferences.PreferencesManager(applicationContext).setTtsHighlightDelay(delayMs)
                    } catch (_: Exception) {}
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun speakCurrentParagraph() {
        if (!isTtsReady || paragraphs.isEmpty()) return
        val firstText = customFirstText
        customFirstText = null
        val charOffset = customFirstCharOffset
        customFirstCharOffset = 0
        currentBlockCharOffset = charOffset

        val fullText = paragraphs.getOrNull(currentIndex)?.trim() ?: return
        val textToSpeak = (firstText ?: fullText).trim()
        if (textToSpeak.isBlank()) {
            nextParagraph()
            return
        }

        val utteranceId = "aura_utterance_${utteranceCounter.incrementAndGet()}_$currentIndex"
        currentActiveUtteranceId = utteranceId

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

        _ttsState.value = _ttsState.value.copy(
            isPlaying = true,
            currentParagraphIndex = currentIndex,
            currentOriginalBlockIndex = originalBlockIndices.getOrNull(currentIndex) ?: currentIndex,
            currentText = fullText,
            currentWordStart = -1,
            currentWordEnd = -1,
            currentSpokenWord = ""
        )

        val notification = buildNotification(isPlaying = true)
        startForeground(NOTIFICATION_ID, notification)
        updateMediaSessionState(isPlaying = true)
    }

    private fun drainHighlightChannel() {
        while (!highlightChannel.isEmpty) {
            highlightChannel.tryReceive()
        }
    }

    private fun pausePlayback() {
        currentActiveUtteranceId = null
        tts?.stop()
        drainHighlightChannel()
        abandonAudioFocus()
        _ttsState.value = _ttsState.value.copy(
            isPlaying = false,
            currentWordStart = -1,
            currentWordEnd = -1,
            currentSpokenWord = ""
        )

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
        currentBlockCharOffset = 0
        drainHighlightChannel()
        if (currentIndex < paragraphs.size - 1) {
            currentIndex++
            speakCurrentParagraph()
        } else {
            // Finished chapter
            _ttsState.value = _ttsState.value.copy(
                chapterFinishedEvent = System.currentTimeMillis()
            )
            pausePlayback()
        }
    }

    private fun previousParagraph() {
        currentBlockCharOffset = 0
        drainHighlightChannel()
        if (currentIndex > 0) {
            currentIndex--
            speakCurrentParagraph()
        } else {
            speakCurrentParagraph()
        }
    }

    private fun stopPlayback() {
        currentActiveUtteranceId = null
        tts?.stop()
        drainHighlightChannel()
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
        val playPauseTitle = if (isPlaying) getString(R.string.tts_action_pause) else getString(R.string.tts_action_play)
        val prevTitle = getString(R.string.tts_action_prev)
        val nextTitle = getString(R.string.tts_action_next)
        val stopTitle = getString(R.string.tts_action_stop)

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(bookTitle.ifBlank { "Aura Reader" })
            .setContentText(currentSnippet.ifBlank { chapterTitle })
            .setSubText(chapterTitle)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_previous, prevTitle, prevIntent).build())
            .addAction(Notification.Action.Builder(playPauseIcon, playPauseTitle, playPauseIntent).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_next, nextTitle, nextIntent).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, stopTitle, stopIntent).build())
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
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
