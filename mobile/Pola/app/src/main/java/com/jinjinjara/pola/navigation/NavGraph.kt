package com.jinjinjara.pola.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.jinjinjara.pola.presentation.ui.screen.MainScreen
import com.jinjinjara.pola.presentation.ui.screen.category.CategoryScreen
import com.jinjinjara.pola.presentation.ui.screen.favorite.FavoriteScreen
import com.jinjinjara.pola.presentation.ui.screen.home.HomeScreen
import com.jinjinjara.pola.presentation.ui.screen.my.MyScreen
import com.jinjinjara.pola.presentation.ui.screen.remind.RemindScreen
import com.jinjinjara.pola.presentation.ui.screen.search.ChatbotScreen
import com.jinjinjara.pola.presentation.ui.screen.search.SearchScreen
import com.jinjinjara.pola.presentation.ui.screen.start.CategorySelectScreen
import com.jinjinjara.pola.presentation.ui.screen.start.StartScreen
import com.jinjinjara.pola.presentation.ui.screen.start.TagSelectScreen
import com.jinjinjara.pola.presentation.ui.screen.tag.TagScreen
import com.jinjinjara.pola.presentation.ui.screen.timeline.TimelineScreen
import com.jinjinjara.pola.presentation.ui.screen.upload.UploadScreen

/**
 * Auth 네비게이션 그래프
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
) {
    navigation(
        startDestination = Screen.Start.route,
        route = NavGraphs.AUTH
    ) {
        // 시작 화면 (구글 로그인) 화면
        composable(route = Screen.Start.route) {
            StartScreen(onLoginSuccess = {
                val isCategorySelected = false

                if (isCategorySelected) {
                    // 카테고리 이미 선택됨 -> 바로 메인으로
                    navController.navigate(NavGraphs.MAIN) {
                        popUpTo(NavGraphs.AUTH) { inclusive = true }
                    }
                } else {
                    // 카테고리 선택 필요 -> 카테고리 선택 화면으로
                    navController.navigate(Screen.CategorySelect.route)
                }
            })
        }

        // 카테고리 선택 화면
        composable(route = Screen.CategorySelect.route) {
            CategorySelectScreen(
                onCategorySelected = {
                    // 태그 선택 화면으로 이동
                    navController.navigate(Screen.TagSelect.route)
                }
            )
        }

        // 태그 선택 화면 추가
        composable(route = Screen.TagSelect.route) {
            TagSelectScreen(
                onNextClick = { selectedTags ->
                    // 태그 선택 완료 후 메인으로 이동
                    navController.navigate(NavGraphs.MAIN) {
                        popUpTo(NavGraphs.AUTH) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Main 네비게이션 그래프
 */
fun NavGraphBuilder.mainNavGraph() {
    composable(route = NavGraphs.MAIN) {
        MainScreen()
    }
}

/**
 * Home 탭 네비게이션 그래프
 */
fun NavGraphBuilder.homeTabGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Home.route,
        route = NavGraphs.HOME_TAB
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCategory = { categoryName ->
                    navController.navigate(Screen.Category.createRoute(categoryName))
                },
                onNavigateToFavorite = {
                    navController.navigate(Screen.Favorite.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.SearchScreen.route)
                },
                onNavigateToChatbot = {
                    navController.navigate(Screen.Chatbot.route)
                }
            )
        }

        composable(Screen.SearchScreen.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onTagClick = { tagName ->
                    navController.navigate(Screen.Tag.createRoute(tagName.removePrefix("#")))
                },
                onSearchClick = { searchQuery ->
                    // TODO: 검색 버튼 클릭 시 동작 구현
                }
            )
        }

        composable(Screen.Chatbot.route) {
            ChatbotScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Home Detail 화면 예시
        composable(
            route = Screen.HomeDetail.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            // TODO: HomeDetailScreen 구현 후 연결
            // HomeDetailScreen(
            //     itemId = itemId,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }
        composable(
            route = Screen.Category.route,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryScreen(
                categoryName = categoryName,
                onBackClick = { navController.popBackStack() },
                onNavigateToTag = { tagName ->
                    navController.navigate(Screen.Tag.createRoute(tagName))
                }
            )
        }

        composable(
            route = Screen.Tag.route,
            arguments = listOf(
                navArgument("tagName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tagName = backStackEntry.arguments?.getString("tagName") ?: ""
            TagScreen(
                tagName = tagName,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Favorite.route) {
            FavoriteScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}

/**
 * Timeline 탭 네비게이션 그래프
 */
fun NavGraphBuilder.timelineTabGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Timeline.route,
        route = NavGraphs.TIMELINE_TAB
    ) {
        composable(Screen.Timeline.route) {
            TimelineScreen(
                // 필요한 네비게이션 콜백 추가
                // onNavigateToDetail = { postId ->
                //     navController.navigate(Screen.TimelineDetail.createRoute(postId))
                // }
            )
        }

        // Timeline Detail 화면 예시
        composable(
            route = Screen.TimelineDetail.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            // TODO: TimelineDetailScreen 구현 후 연결
            // TimelineDetailScreen(
            //     postId = postId,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }
    }
}

/**
 * My 탭 네비게이션 그래프
 */
fun NavGraphBuilder.myTabGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.My.route,
        route = NavGraphs.MY_TAB
    ) {
        composable(Screen.My.route) {
            MyScreen(
                // 필요한 네비게이션 콜백 추가
                // onNavigateToProfile = {
                //     navController.navigate(Screen.Profile.route)
                // },
                // onNavigateToSettings = {
                //     navController.navigate(Screen.Settings.route)
                // }
            )
        }

        // Profile 화면
        composable(Screen.Profile.route) {
            // TODO: ProfileScreen 구현 후 연결
            // ProfileScreen(
            //     onNavigateBack = { navController.popBackStack() },
            //     onNavigateToEdit = {
            //         navController.navigate(Screen.EditProfile.route)
            //     }
            // )
        }

        // Settings 화면
        composable(Screen.Settings.route) {
            // TODO: SettingsScreen 구현 후 연결
            // SettingsScreen(
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }

        // Edit Profile 화면
        composable(Screen.EditProfile.route) {
            // TODO: EditProfileScreen 구현 후 연결
            // EditProfileScreen(
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }
    }
}

/**
 * Upload 화면 (단일 화면)
 */
fun NavGraphBuilder.uploadScreen(navController: NavHostController) {
    composable(Screen.Upload.route) {
        UploadScreen(
            onBackClick = { navController.popBackStack() },
            onClipboardClick = {

            },
            onCameraClick = {

            }
        )
    }
}

/**
 * Remind 화면 (단일 화면)
 */
fun NavGraphBuilder.remindScreen() {
    composable(Screen.Remind.route) {
        RemindScreen()
    }
}