package com.aura.reader.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aura.reader.ui.screens.library.LibraryScreen
import com.aura.reader.ui.screens.library.LibraryViewModel
import com.aura.reader.ui.screens.opds.OpdsScreen
import com.aura.reader.ui.screens.opds.OpdsViewModel
import com.aura.reader.ui.screens.reader.ReaderScreen
import com.aura.reader.ui.screens.reader.ReaderViewModel

object NavRoutes {
    const val LIBRARY = "library"
    const val READER = "reader"
    const val CATALOG = "catalog"
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AuraNavGraph(
    navController: NavHostController = rememberNavController(),
    libraryViewModel: LibraryViewModel,
    readerViewModel: ReaderViewModel,
    opdsViewModel: OpdsViewModel
) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
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
                    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                        LibraryScreen(
                            viewModel = libraryViewModel,
                            onBookSelected = { book ->
                                readerViewModel.openBook(book)
                                navController.navigate(NavRoutes.READER)
                            },
                            onOpenCatalog = {
                                navController.navigate(NavRoutes.CATALOG)
                            }
                        )
                    }
                }

                composable(NavRoutes.READER) {
                    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                        ReaderScreen(
                            viewModel = readerViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                composable(NavRoutes.CATALOG) {
                    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                        OpdsScreen(
                            viewModel = opdsViewModel,
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
        }
    }
}
