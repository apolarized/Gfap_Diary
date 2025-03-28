package com.example.diplomatiki.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diplomatiki.ui.graph.GraphDestination
import com.example.diplomatiki.ui.graph.GraphScreen
import com.example.diplomatiki.ui.history.HistoryDestination
import com.example.diplomatiki.ui.history.HistoryScreen
import com.example.diplomatiki.ui.home.HomeDestination
import com.example.diplomatiki.ui.home.HomeScreen
import com.example.diplomatiki.ui.item.ItemDetailsDestination
import com.example.diplomatiki.ui.item.ItemDetailsScreen
import com.example.diplomatiki.ui.item.ItemEditDestination
import com.example.diplomatiki.ui.item.ItemEditScreen
import com.example.diplomatiki.ui.item.ItemEntryDestination
import com.example.diplomatiki.ui.item.ItemEntryScreen
import com.example.diplomatiki.ui.settings.SettingsDestination
import com.example.diplomatiki.ui.settings.SettingsScreen
import com.example.diplomatiki.ui.statistics.StatisticsDestination
import com.example.diplomatiki.ui.statistics.StatisticsScreen

// Define reusable transitions - simple fade animations
private val enterTransition: EnterTransition = fadeIn(
    animationSpec = tween(durationMillis = 300, easing = EaseInOut)
)

private val exitTransition: ExitTransition = fadeOut(
    animationSpec = tween(durationMillis = 300, easing = EaseInOut)
)

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun DiplomatikiNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(
            route = HomeDestination.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { enterTransition },
            popExitTransition = { exitTransition }
        ) {
            HomeScreen(
                navigateToItemEntry = { navController.navigate(ItemEntryDestination.route) },
                navigateToHistory = { navController.navigate(HistoryDestination.route)},
                navigateToGraph = { navController.navigate(GraphDestination.route) },
                navigateToSettings = { navController.navigate(SettingsDestination.route) },
                navigateToStatistics = { navController.navigate(StatisticsDestination.route) }
                )
        }
        composable(
            route = HistoryDestination.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { enterTransition },
            popExitTransition = { exitTransition }
        ) {
            HistoryScreen(
                navigateToItemEntry = { navController.navigate(ItemEntryDestination.route) },
                navigateToItemUpdate = {
                    navController.navigate("${ItemDetailsDestination.route}/${it}")
                },
                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToGraph = { navController.navigate(GraphDestination.route) },
                navigateToSettings = { navController.navigate(SettingsDestination.route) },
                navigateToStatistics = { navController.navigate(StatisticsDestination.route) }
            )
        }
        composable(
            route = ItemEntryDestination.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { enterTransition },
            popExitTransition = { exitTransition }
        ) {
            ItemEntryScreen(
                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToHistory = { navController.navigate(HistoryDestination.route)},
                navigateToGraph = { navController.navigate(GraphDestination.route) },
                navigateToStatistics = { navController.navigate(StatisticsDestination.route) },
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = ItemDetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(ItemDetailsDestination.itemIdArg) {
                type = NavType.IntType
            }),
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { enterTransition },
            popExitTransition = { exitTransition }
        ) {
            ItemDetailsScreen(
                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToHistory = { navController.navigate(HistoryDestination.route)},
                navigateToGraph = { navController.navigate(GraphDestination.route) },
                navigateToStatistics = { navController.navigate(StatisticsDestination.route)},
                navigateToEditItem = { navController.navigate("${ItemEditDestination.route}/$it") },
                navigateBack = { navController.navigateUp() }
            )
        }
        composable(
            route = ItemEditDestination.routeWithArgs,
            arguments = listOf(navArgument(ItemEditDestination.itemIdArg) {
                type = NavType.IntType
            }),
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { enterTransition },
            popExitTransition = { exitTransition }
        ) {
            ItemEditScreen(
                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToHistory = { navController.navigate(HistoryDestination.route)},
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() },
                navigateToGraph = { navController.navigate(GraphDestination.route) },
                navigateToStatistics = { navController.navigate(StatisticsDestination.route) }
            )
        }
        composable(
            route = GraphDestination.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { enterTransition },
            popExitTransition = { exitTransition }
        ) {
            GraphScreen(
                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToHistory = { navController.navigate(HistoryDestination.route)},
                navigateToSettings = { navController.navigate(SettingsDestination.route) },
                navigateToStatistics = { navController.navigate(StatisticsDestination.route) }
            )
        }
        composable(
            route = SettingsDestination.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { enterTransition },
            popExitTransition = { exitTransition }
        ) {
            SettingsScreen(
                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToHistory = { navController.navigate(HistoryDestination.route) },
                navigateToGraph = { navController.navigate(GraphDestination.route) },
                navigateBack = { navController.navigateUp() },
                navigateToStatistics = { navController.navigate(StatisticsDestination.route) }
            )
        }
        composable(
            route = StatisticsDestination.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { enterTransition },
            popExitTransition = { exitTransition }
        ) {
            StatisticsScreen(
                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToHistory = { navController.navigate(HistoryDestination.route) },
                navigateToGraph = { navController.navigate(GraphDestination.route) },
                navigateToSettings = { navController.navigate(SettingsDestination.route) }
            )
        }
    }
}
