package com.jinjinjara.pola.presentation.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    pendingNavigationFileId: Long? = null,
    onNavigationHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 위젯에서 전달받은 fileId가 있으면 해당 콘텐츠 화면으로 이동
    LaunchedEffect(pendingNavigationFileId) {
        android.util.Log.d("Widget", "[Widget] MainScreen - LaunchedEffect triggered")
        android.util.Log.d("Widget", "[Widget] MainScreen - pendingNavigationFileId: $pendingNavigationFileId")

        pendingNavigationFileId?.let { fileId ->
            val route = Screen.Contents.createRoute(fileId)
            android.util.Log.d("Widget", "[Widget] MainScreen - Navigating to: $route")
            navController.navigate(route)
            android.util.Log.d("Widget", "[Widget] MainScreen - Navigation called, invoking onNavigationHandled")
            onNavigationHandled()
            android.util.Log.d("Widget", "[Widget] MainScreen - ✓ Widget navigation completed!")
        } ?: run {
            android.util.Log.d("Widget", "[Widget] MainScreen - No pending navigation")
        }
    }

    // MainViewModel이 카테고리를 체크하고 필요시 DataStore 업데이트
    // PolaNavHost의 LaunchedEffect가 자동으로 네비게이션 처리

    // BottomNavigationBar 보여줄 화면
    val showBottomBar = currentDestination?.route in listOf(
        BottomNavItem.Home.route,
        BottomNavItem.Timeline.route,
//        BottomNavItem.Upload.route,
        BottomNavItem.Remind.route,
        BottomNavItem.My.route,
        Screen.Category.route,
        Screen.Tag.route,
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
                                when (item.route) {
                                    Screen.Home.route -> it.route == NavGraphs.HOME_TAB || it.route == item.route
                                    Screen.Timeline.route -> it.route == NavGraphs.TIMELINE_TAB || it.route == item.route
                                    Screen.My.route -> it.route == NavGraphs.MY_TAB || it.route == item.route
                                    else -> it.route == item.route
                                }
                            } == true

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        val targetRoute = when (item.route) {
                                            Screen.Home.route -> NavGraphs.HOME_TAB
                                            Screen.Timeline.route -> NavGraphs.TIMELINE_TAB
                                            Screen.Upload.route -> Screen.Upload.route
                                            Screen.Remind.route -> Screen.Remind.route
                                            Screen.My.route -> NavGraphs.MY_TAB
                                            else -> item.route
                                        }

                                        navController.navigate(targetRoute) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = when (targetRoute) {
                                                NavGraphs.HOME_TAB,
                                                NavGraphs.TIMELINE_TAB,
                                                Screen.Remind.route,
                                                NavGraphs.MY_TAB -> false
                                                else -> true
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (selected) item.selectedIcon else item.icon
                                        ),
                                        contentDescription = item.title,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
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
            uploadScreen(navController)
            remindScreen(navController)
        }
    }
}