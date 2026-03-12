package com.muzaffer.bistai.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.muzaffer.bistai.presentation.aichat.AiChatScreen
import com.muzaffer.bistai.presentation.favorites.FavoritesScreen
import com.muzaffer.bistai.presentation.portfolio.PortfolioScreen
import com.muzaffer.bistai.presentation.stockdetail.StockDetailScreen
import com.muzaffer.bistai.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BistaiNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Portfolio.route
    ) {
        // ── Ana ekran: 3 sekme + HorizontalPager ──────────────────────
        composable(Screen.Portfolio.route) {
            MainTabsScreen(
                onStockClick = { symbol ->
                    navController.navigate(Screen.StockDetail.createRoute(symbol))
                }
            )
        }

        // ── Hisse Detay (pager dışında, tam ekran) ────────────────────
        composable(
            route = Screen.StockDetail.route,
            arguments = listOf(navArgument("symbol") { type = NavType.StringType })
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            StockDetailScreen(
                symbol = symbol,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ─── Ana Sekmeler (HorizontalPager) ──────────────────────────────────────────

@Composable
private fun MainTabsScreen(onStockClick: (String) -> Unit) {
    val tabs = BottomNavItem.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = PureBlack,
        bottomBar = {
            BistaiBottomBar(
                selectedIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            beyondViewportPageCount = 1   // yan sayfaları bellekte tut, geçiş pürüzsüz olsun
        ) { page ->
            when (tabs[page]) {
                BottomNavItem.PORTFOLIO -> PortfolioScreen(onStockClick = onStockClick)
                BottomNavItem.FAVORITES -> FavoritesScreen(onStockClick = onStockClick)
                BottomNavItem.AI_CHAT   -> AiChatScreen()
            }
        }
    }
}

// ─── Bottom Navigation Bar ───────────────────────────────────────────────────

@Composable
private fun BistaiBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = NavyBlueSurface,
        tonalElevation = 0.dp
    ) {
        BottomNavItem.entries.forEachIndexed { index, item ->
            val selected = selectedIndex == index
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = if (item == BottomNavItem.AI_CHAT) GoldAccent else BullishGreen,
                    selectedTextColor   = if (item == BottomNavItem.AI_CHAT) GoldAccent else BullishGreen,
                    unselectedIconColor = SlateBlue,
                    unselectedTextColor = SlateBlue,
                    indicatorColor      = if (item == BottomNavItem.AI_CHAT)
                        GoldAccent.copy(alpha = 0.12f) else BullishGreen.copy(alpha = 0.12f)
                )
            )
        }
    }
}
