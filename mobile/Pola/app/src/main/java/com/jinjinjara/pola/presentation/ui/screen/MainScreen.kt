package com.jinjinjara.pola.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.jinjinjara.pola.navigation.NavGraphs
import com.jinjinjara.pola.navigation.Screen
import com.jinjinjara.pola.navigation.homeTabGraph
import com.jinjinjara.pola.navigation.myTabGraph
import com.jinjinjara.pola.navigation.remindScreen
import com.jinjinjara.pola.navigation.timelineTabGraph
import com.jinjinjara.pola.navigation.uploadScreen
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

    // BottomNavigationBar 보여줄 화면
    val showBottomBar = currentDestination?.route in listOf(
        BottomNavItem.Home.route,
        BottomNavItem.Timeline.route,
//        BottomNavItem.Upload.route,
        BottomNavItem.Remind.route,
        BottomNavItem.My.route
    )
    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier.navigationBarsPadding()
                ) {
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
                                    val targetRoute = when (item.route) {
                                        Screen.Home.route -> NavGraphs.HOME_TAB
                                        Screen.Timeline.route -> NavGraphs.TIMELINE_TAB
                                        Screen.Upload.route -> Screen.Upload.route
                                        Screen.Remind.route -> Screen.Remind.route
                                        Screen.My.route -> NavGraphs.MY_TAB
                                        else -> item.route
                                    }

                                    navController.navigate(targetRoute) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavGraphs.HOME_TAB,
            modifier = Modifier.padding(innerPadding)
        ) {
            // NavGraph.kt에 정의된 각 탭의 네비게이션 그래프 호출
            homeTabGraph(navController)
            timelineTabGraph(navController)
            myTabGraph(navController)

            // 단일 화면들
            uploadScreen()
            remindScreen()
        }
    }
}