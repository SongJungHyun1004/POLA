package com.jinjinjara.pola.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation

/**
 * Auth 네비게이션 그래프
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
) {
    navigation(
        startDestination = Screen.Login.route,
        route = NavGraphs.AUTH
    ) {
        // 로그인 화면
        composable(route = Screen.Login.route) {
            // TODO: LoginScreen 구현 후 연결
            // LoginScreen(
            //     onNavigateToSignUp = {
            //         navController.navigate(Screen.SignUp.route)
            //     },
            //     onLoginSuccess = onLoginSuccess
            // )
        }

        // 회원가입 화면
        composable(route = Screen.SignUp.route) {
            // TODO: SignUpScreen 구현 후 연결
            // SignUpScreen(
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     },
            //     onSignUpSuccess = {
            //         navController.popBackStack()
            //     }
            // )
        }
    }
}

/**
 * Main 네비게이션 그래프
 */
fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = Screen.Home.route,
        route = NavGraphs.MAIN
    ) {
        // 홈 화면
        composable(route = Screen.Home.route) {
            // TODO: HomeScreen 구현 후 연결
            // HomeScreen(
            //     onNavigateToProfile = {
            //         navController.navigate(Screen.Profile.route)
            //     },
            //     onNavigateToSettings = {
            //         navController.navigate(Screen.Settings.route)
            //     },
            //     onNavigateToDetail = { itemId ->
            //         navController.navigate(Screen.Detail.createRoute(itemId))
            //     }
            // )
        }

        // 프로필 화면
        composable(route = Screen.Profile.route) {
            // TODO: ProfileScreen 구현 후 연결
            // ProfileScreen(
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     }
            // )
        }

        // 설정 화면
        composable(route = Screen.Settings.route) {
            // TODO: SettingsScreen 구현 후 연결
            // SettingsScreen(
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     },
            //     onLogout = {
            //         navController.navigate(NavGraphs.AUTH) {
            //             popUpTo(NavGraphs.MAIN) { inclusive = true }
            //         }
            //     }
            // )
        }

        // 상세 화면 (인자 받기 예시)
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            // TODO: DetailScreen 구현 후 연결
            // DetailScreen(
            //     itemId = itemId,
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     }
            // )
        }

        // 검색 화면 (선택적 인자 예시)
        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query")
            // TODO: SearchScreen 구현 후 연결
            // SearchScreen(
            //     initialQuery = query,
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     }
            // )
        }
    }
}