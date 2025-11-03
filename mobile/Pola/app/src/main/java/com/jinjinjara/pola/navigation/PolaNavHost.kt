package com.jinjinjara.pola.navigation

import androidx.compose.runtime.Composable
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
 */
@Composable
fun PolaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    isLoggedIn: Boolean = false,
) {

    // 테스트용: 이미지 클릭 시 토큰 없이 메인으로 이동
    var isTestMode by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn || isTestMode) NavGraphs.MAIN else NavGraphs.AUTH,
        modifier = modifier
    ) {
        // Auth 네비게이션 그래프
        authNavGraph(
            navController = navController
        )

        // Main 네비게이션 그래프
        mainNavGraph()
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