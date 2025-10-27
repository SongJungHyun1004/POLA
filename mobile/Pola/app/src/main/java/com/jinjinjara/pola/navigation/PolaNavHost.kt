package com.jinjinjara.pola.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

/**
 * 앱의 메인 네비게이션 호스트
 *
 * @param modifier Modifier
 * @param navController NavHostController (외부에서 주입 가능)
 * @param startDestination 시작 화면 (로그인 상태에 따라 변경)
 */
@Composable
fun PolaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavGraphs.AUTH, // 기본은 로그인 화면
) {
    // 로그인 상태 관리 (실제로는 ViewModel이나 DataStore에서 관리)
    var isLoggedIn by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) NavGraphs.MAIN else NavGraphs.AUTH,
        modifier = modifier
    ) {
        // Auth 네비게이션 그래프
        authNavGraph(
            navController = navController,
            onLoginSuccess = {
                isLoggedIn = true
                navController.navigate(NavGraphs.MAIN) {
                    // 뒤로가기 시 로그인 화면으로 안 가도록 설정
                    popUpTo(NavGraphs.AUTH) { inclusive = true }
                }
            }
        )

        // Main 네비게이션 그래프
        mainNavGraph(
            navController = navController
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