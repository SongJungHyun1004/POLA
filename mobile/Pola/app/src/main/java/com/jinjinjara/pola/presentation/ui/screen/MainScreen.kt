package com.jinjinjara.pola.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jinjinjara.pola.navigation.BottomNavItem
import com.jinjinjara.pola.presentation.ui.screen.home.HomeScreen
import com.jinjinjara.pola.presentation.ui.screen.my.MyScreen
import com.jinjinjara.pola.presentation.ui.screen.remind.RemindScreen
import com.jinjinjara.pola.presentation.ui.screen.timeline.TimelineScreen
import com.jinjinjara.pola.presentation.ui.screen.upload.UploadScreen

/**
 * Bottom Navigation이 있는 메인 화면
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                )
                NavigationBar(
                    modifier = Modifier.height(64.dp),
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    BottomNavItem.items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (selected) item.selectedIcon else item.icon
                                        ),
                                        contentDescription = item.title,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // 시작 destination으로 팝업하여 스택 관리
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // 같은 아이템을 다시 선택했을 때 중복 방지
                                    launchSingleTop = true
                                    // 이전 상태 복원
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Unspecified,
                                unselectedIconColor = Color.Unspecified,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen()
            }

            composable(BottomNavItem.Timeline.route) {
                TimelineScreen()
            }

            composable(BottomNavItem.Upload.route) {
                UploadScreen()
            }

            composable(BottomNavItem.Remind.route) {
                RemindScreen()
            }

            composable(BottomNavItem.My.route) {
                MyScreen()
            }
        }
    }
}