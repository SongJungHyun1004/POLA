package com.jinjinjara.pola.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jinjinjara.pola.presentation.ui.screen.MainScreen

/**
 * 앱의 메인 네비게이션 호스트
 *
 * @param modifier Modifier
 * @param navController NavHostController (외부에서 주입 가능)
 * @param startDestination 시작 화면 (로그인 상태에 따라 변경)
 * @param pendingNavigationFileId 위젯에서 전달받은 콘텐츠 ID (nullable)
 * @param onNavigationHandled 네비게이션 처리 완료 후 콜백
 */
@Composable
fun PolaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    isLoggedIn: Boolean = false,
    onboardingCompleted: Boolean = false,
    pendingNavigationFileId: Long? = null,
    onNavigationHandled: () -> Unit = {}
) {

    // 테스트용: 이미지 클릭 시 토큰 없이 메인으로 이동
    var isTestMode by remember { mutableStateOf(false) }

    // 로그인 상태와 온보딩 상태에 따라 동적으로 네비게이션
    LaunchedEffect(isLoggedIn, onboardingCompleted) {
        Log.d("PolaNavHost", "State changed - isLoggedIn: $isLoggedIn, onboardingCompleted: $onboardingCompleted")
        Log.d("Widget", "[Widget] PolaNavHost - pendingNavigationFileId: $pendingNavigationFileId")

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        Log.d("PolaNavHost", "Current route: $currentRoute")

        when {
            isLoggedIn && onboardingCompleted -> {
                // 로그인 && 온보딩 완료 -> 메인으로 이동
                if (currentRoute != NavGraphs.MAIN) {
                    Log.d("PolaNavHost", "Navigating to MAIN")
                    navController.navigate(NavGraphs.MAIN) {
                        popUpTo(NavGraphs.AUTH) { inclusive = true }
                    }
                }
            }
            isLoggedIn && !onboardingCompleted -> {
                // 로그인 완료 but 온보딩 미완료 -> 카테고리 선택으로
                if (currentRoute != Screen.CategorySelect.route && currentRoute != Screen.TagSelect.route) {
                    Log.d("PolaNavHost", "Navigating to CategorySelect")
                    navController.navigate(Screen.CategorySelect.route) {
                        popUpTo(NavGraphs.AUTH) { inclusive = false }
                    }
                }
            }
            !isLoggedIn -> {
                // 로그인 안됨 -> 시작 화면으로
                if (currentRoute != Screen.Start.route) {
                    Log.d("PolaNavHost", "Navigating to Start")
                    navController.navigate(Screen.Start.route) {
                        popUpTo(NavGraphs.AUTH) { inclusive = false }
                    }
                }
            }
        }
    }

    // 항상 AUTH에서 시작 (LaunchedEffect가 적절한 화면으로 이동시킴)
    NavHost(
        navController = navController,
        startDestination = NavGraphs.AUTH,
        modifier = modifier
    ) {
        // Auth 네비게이션 그래프
        authNavGraph(
            navController = navController,
            isLoggedIn = isLoggedIn,
            onboardingCompleted = onboardingCompleted
        )

        // Main 네비게이션 그래프
        mainNavGraph(
            navController = navController,
            pendingNavigationFileId = pendingNavigationFileId,
            onNavigationHandled = onNavigationHandled
        )
    }
}

/**
 * 네비게이션 확장 함수들
 */

/**
 * 안전한 네비게이션 (중복 네비게이션 방지)
 */
fun NavHostController.navigateSafe(route: String) {
    if (currentDestination?.route != route) {
        navigate(route)
    }
}

/**
 * 싱글 톱으로 네비게이션 (스택에 중복 방지)
 */
fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

/**
 * 특정 화면까지 스택 제거하고 네비게이션
 */
fun NavHostController.navigateAndClear(route: String, popUpToRoute: String) {
    navigate(route) {
        popUpTo(popUpToRoute) { inclusive = true }
    }
}