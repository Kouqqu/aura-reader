package com.aura.reader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aura.reader.ui.screens.library.LibraryScreen
import com.aura.reader.ui.screens.library.LibraryViewModel
import com.aura.reader.ui.screens.reader.ReaderScreen
import com.aura.reader.ui.screens.reader.ReaderViewModel

object NavRoutes {
    const val LIBRARY = "library"
    const val READER = "reader"
}

@Composable
fun AuraNavGraph(
    navController: NavHostController = rememberNavController(),
    libraryViewModel: LibraryViewModel,
    readerViewModel: ReaderViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.LIBRARY
    ) {
        composable(NavRoutes.LIBRARY) {
            LibraryScreen(
                viewModel = libraryViewModel,
                onBookSelected = { book ->
                    readerViewModel.openBook(book)
                    navController.navigate(NavRoutes.READER)
                }
            )
        }

        composable(NavRoutes.READER) {
            ReaderScreen(
                viewModel = readerViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
