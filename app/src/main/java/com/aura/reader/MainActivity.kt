package com.aura.reader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
import com.aura.reader.ui.navigation.AuraNavGraph
import com.aura.reader.ui.navigation.NavRoutes
import com.aura.reader.ui.screens.library.LibraryViewModel
import com.aura.reader.ui.screens.reader.ReaderViewModel
import com.aura.reader.ui.theme.AuraReaderTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var bookRepository: BookRepository

    private val libraryViewModel: LibraryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LibraryViewModel(bookRepository) as T
            }
        }
    }

    private val readerViewModel: ReaderViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReaderViewModel(bookRepository, preferencesManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(applicationContext)
        bookRepository = BookRepository(applicationContext, preferencesManager)

        // Handle opening a book from external intent (e.g. file manager)
        handleIncomingIntent(intent)

        setContent {
            AuraReaderTheme {
                val navController = rememberNavController()
                AuraNavGraph(
                    navController = navController,
                    libraryViewModel = libraryViewModel,
                    readerViewModel = readerViewModel
                )
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
                libraryViewModel.openBookFromUri(uri)
            }
        }
    }
}
