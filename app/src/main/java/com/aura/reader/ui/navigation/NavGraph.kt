package com.aura.reader.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aura.reader.ui.screens.flibusta.FlibustaScreen
import com.aura.reader.ui.screens.flibusta.FlibustaViewModel
import com.aura.reader.ui.screens.library.LibraryScreen
import com.aura.reader.ui.screens.library.LibraryViewModel
import com.aura.reader.ui.screens.reader.ReaderScreen
import com.aura.reader.ui.screens.reader.ReaderViewModel

object NavRoutes {
    const val LIBRARY = "library"
    const val READER = "reader"
    const val FLIBUSTA = "flibusta"
}

@Composable
fun AuraNavGraph(
    navController: NavHostController = rememberNavController(),
    libraryViewModel: LibraryViewModel,
    readerViewModel: ReaderViewModel,
    flibustaViewModel: FlibustaViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.LIBRARY,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable(NavRoutes.LIBRARY) {
            LibraryScreen(
                viewModel = libraryViewModel,
                onBookSelected = { book ->
                    readerViewModel.openBook(book)
                    navController.navigate(NavRoutes.READER)
                },
                onOpenFlibusta = {
                    navController.navigate(NavRoutes.FLIBUSTA)
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

        composable(NavRoutes.FLIBUSTA) {
            FlibustaScreen(
                viewModel = flibustaViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenBook = { book ->
                    readerViewModel.openBook(book)
                    navController.navigate(NavRoutes.READER)
                }
            )
        }
    }
}
