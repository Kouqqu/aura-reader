package com.aura.reader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.navigation.compose.rememberNavController
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
import com.aura.reader.ui.navigation.AuraNavGraph
import com.aura.reader.ui.navigation.NavRoutes
import com.aura.reader.ui.screens.library.LibraryViewModel
import com.aura.reader.ui.screens.reader.ReaderViewModel
import com.aura.reader.ui.theme.AuraReaderTheme

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.ui.theme.AppLanguage
import com.aura.reader.ui.theme.LocalAppStrings
import com.aura.reader.ui.theme.getStrings

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.aura.reader.data.analytics.AnalyticsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var bookRepository: BookRepository
    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var readerViewModel: ReaderViewModel
    private lateinit var opdsViewModel: com.aura.reader.ui.screens.opds.OpdsViewModel

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Takes over the system starting window; must run before super.onCreate().
        val splashScreen = installSplashScreen()

        // Compose 1.12 starts using a new context-menu implementation for text
        // selection, and that path ignores the app's custom LocalTextToolbar.
        // The reader relies on that toolbar for its quote/dictionary/translate/
        // TTS actions, so keep the classic selection toolbar for the whole app.
        ComposeFoundationFlags.isNewContextMenuEnabled = false
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(applicationContext)
        bookRepository = BookRepository(applicationContext, preferencesManager)
        libraryViewModel = LibraryViewModel(bookRepository, preferencesManager)
        readerViewModel = ReaderViewModel(bookRepository, preferencesManager, applicationContext)
        opdsViewModel = com.aura.reader.ui.screens.opds.OpdsViewModel(bookRepository, preferencesManager)

        // Track anonymous app metrics if enabled
        AnalyticsManager.trackAppLaunch(
            context = applicationContext,
            preferencesManager = preferencesManager,
            scope = lifecycleScope
        )

        // Handle opening a book from external intent (e.g. file manager)
        handleIncomingIntent(intent)

        // Read the onboarding flag off the main thread. The old blocking read froze
        // the first frame, and the app drew a blank white window until it finished.
        // The system splash stays on screen until the value is known, so the user
        // only ever sees the logo, then the real screen.
        val onboardingState = MutableStateFlow<Boolean?>(null)
        splashScreen.setKeepOnScreenCondition { onboardingState.value == null }
        lifecycleScope.launch {
            val completed = try {
                withTimeoutOrNull(3_000) {
                    preferencesManager.hasCompletedOnboarding.first()
                }
            } catch (error: Exception) {
                null
            }
            // If the preference cannot be read in time, open the library rather
            // than hanging on the splash; onboarding is shown on the next launch.
            onboardingState.value = completed ?: true
        }

        setContent {
            val hasCompletedOnboarding by onboardingState.collectAsState()
            val settings by preferencesManager.readerSettings.collectAsState(
                initial = ReaderSettings()
            )
            val appLanguage by preferencesManager.appLanguage.collectAsState(
                initial = AppLanguage.RU
            )
            val materialYouEnabled by preferencesManager.materialYouEnabled.collectAsState(
                initial = false
            )
            val appFont by preferencesManager.appFont.collectAsState(
                initial = "DEFAULT"
            )

            val completed = hasCompletedOnboarding
            if (completed != null) {
                CompositionLocalProvider(
                    LocalAppStrings provides getStrings(appLanguage)
                ) {
                    AuraReaderTheme(
                        themeMode = settings.appThemeMode,
                        materialYou = materialYouEnabled,
                        appFont = appFont
                    ) {
                        val navController = rememberNavController()
                        AuraNavGraph(
                            navController = navController,
                            libraryViewModel = libraryViewModel,
                            readerViewModel = readerViewModel,
                            opdsViewModel = opdsViewModel,
                            preferencesManager = preferencesManager,
                            startDestination = if (completed) {
                                NavRoutes.LIBRARY
                            } else {
                                NavRoutes.ONBOARDING
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                if (::libraryViewModel.isInitialized) {
                    libraryViewModel.openBookFromUri(uri)
                }
            }
        }
    }
}
