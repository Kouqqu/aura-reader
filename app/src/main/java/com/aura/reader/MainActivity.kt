package com.aura.reader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
import com.aura.reader.ui.navigation.AuraNavGraph
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

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var bookRepository: BookRepository
    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var readerViewModel: ReaderViewModel
    private lateinit var flibustaViewModel: com.aura.reader.ui.screens.flibusta.FlibustaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(applicationContext)
        bookRepository = BookRepository(applicationContext, preferencesManager)
        libraryViewModel = LibraryViewModel(bookRepository, preferencesManager)
        readerViewModel = ReaderViewModel(bookRepository, preferencesManager)
        flibustaViewModel = com.aura.reader.ui.screens.flibusta.FlibustaViewModel(bookRepository, preferencesManager)

        // Handle opening a book from external intent (e.g. file manager)
        handleIncomingIntent(intent)

        setContent {
            val settings by preferencesManager.readerSettings.collectAsState(
                initial = ReaderSettings()
            )
            val appLanguage by preferencesManager.appLanguage.collectAsState(
                initial = AppLanguage.RU
            )
            val materialYouEnabled by preferencesManager.materialYouEnabled.collectAsState(
                initial = false
            )

            CompositionLocalProvider(
                LocalAppStrings provides getStrings(appLanguage)
            ) {
                AuraReaderTheme(
                    themeMode = settings.themeMode,
                    materialYou = materialYouEnabled
                ) {
                    val navController = rememberNavController()
                    AuraNavGraph(
                        navController = navController,
                        libraryViewModel = libraryViewModel,
                        readerViewModel = readerViewModel,
                        flibustaViewModel = flibustaViewModel
                    )
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
