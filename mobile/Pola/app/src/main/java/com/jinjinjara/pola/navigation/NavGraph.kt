package com.jinjinjara.pola.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import com.jinjinjara.pola.domain.model.Tag
import com.jinjinjara.pola.presentation.ui.screen.start.CategorySelectViewModel
import com.jinjinjara.pola.presentation.ui.screen.start.TagSelectViewModel
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import com.jinjinjara.pola.presentation.ui.screen.MainScreen
import com.jinjinjara.pola.presentation.ui.screen.category.CategoryScreen
import com.jinjinjara.pola.presentation.ui.screen.contents.ContentsEditScreen
import com.jinjinjara.pola.presentation.ui.screen.contents.ContentsScreen
import com.jinjinjara.pola.presentation.ui.screen.favorite.FavoriteScreen
import com.jinjinjara.pola.presentation.ui.screen.home.HomeScreen
import com.jinjinjara.pola.presentation.ui.screen.my.EditCategoryScreen
import com.jinjinjara.pola.presentation.ui.screen.my.EditCategoryViewModel
import com.jinjinjara.pola.presentation.ui.screen.my.EditTagScreen
import com.jinjinjara.pola.presentation.ui.screen.my.EditTagUiState
import com.jinjinjara.pola.presentation.ui.screen.my.EditTagViewModel
import com.jinjinjara.pola.presentation.ui.screen.my.MyScreen
import com.jinjinjara.pola.presentation.ui.screen.my.MyTypeScreen
import com.jinjinjara.pola.presentation.ui.screen.my.TermsOfServiceScreen
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
 * DataStore EntryPoint for accessing PreferencesDataStore in Composables
 */
@EntryPoint
@InstallIn(ActivityComponent::class)
interface DataStoreEntryPoint {
    fun preferencesDataStore(): PreferencesDataStore
}

/**
 * Auth 네비게이션 그래프
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean = false,
    onboardingCompleted: Boolean = false
) {
    // Auth 시작 화면 결정:
    // - 로그인 완료 && 온보딩 미완료: CategorySelect (온보딩 필요)
    // - 로그인 미완료: Start (로그인 필요)
    val authStartDestination = if (isLoggedIn && !onboardingCompleted) {
        Screen.CategorySelect.route
    } else {
        Screen.Start.route
    }

    navigation(
        startDestination = authStartDestination,
        route = NavGraphs.AUTH
    ) {
        // 시작 화면 (구글 로그인) 화면
        composable(route = Screen.Start.route) {
            StartScreen(
                onLoginSuccess = { onboardingCompleted ->
                    // PolaNavHost의 LaunchedEffect가 자동으로 네비게이션 처리
                    // 여기서는 아무것도 하지 않음 (DataStore 상태 변경으로 자동 이동)
                }
            )
        }

        // 카테고리 선택 화면
        composable(route = Screen.CategorySelect.route) {
            // AUTH 네비게이션 그래프 스코프의 공유 ViewModel 사용
            val authBackStackEntry = remember(it) {
                navController.getBackStackEntry(NavGraphs.AUTH)
            }
            val sharedViewModel: CategorySelectViewModel = hiltViewModel(authBackStackEntry)

            CategorySelectScreen(
                viewModel = sharedViewModel,
                onCategorySelected = { categoriesWithTags ->
                    // 선택된 카테고리 정보를 저장 (navController의 savedStateHandle 사용)
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "categoriesWithTags",
                        categoriesWithTags
                    )
                    // 태그 선택 화면으로 이동
                    navController.navigate(Screen.TagSelect.route)
                }
            )
        }

        // 태그 선택 화면 추가
        composable(route = Screen.TagSelect.route) {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            // AUTH 그래프 스코프의 ViewModel 사용 (상태 유지)
            val authBackStackEntry = remember(it) {
                navController.getBackStackEntry(NavGraphs.AUTH)
            }
            val tagSelectViewModel: TagSelectViewModel = hiltViewModel(authBackStackEntry)

            // 이전 화면에서 전달받은 선택된 카테고리 정보
            val categoriesWithTags = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Map<String, List<Tag>>>("categoriesWithTags")
                ?: emptyMap()

            TagSelectScreen(
                categoriesWithTags = categoriesWithTags,
                viewModel = tagSelectViewModel,
                onNextClick = {
                    // 온보딩 완료 플래그 저장
                    // PolaNavHost의 LaunchedEffect가 자동으로 MAIN으로 네비게이션 처리
                    coroutineScope.launch {
                        val entryPoint = EntryPointAccessors.fromActivity(
                            context as android.app.Activity,
                            DataStoreEntryPoint::class.java
                        )
                        entryPoint.preferencesDataStore().setOnboardingCompleted(true)
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
@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController,
    pendingNavigationFileId: Long? = null,
    onNavigationHandled: () -> Unit = {}
) {
    composable(route = NavGraphs.MAIN) {
        MainScreen(
            pendingNavigationFileId = pendingNavigationFileId,
            onNavigationHandled = onNavigationHandled
        )
        // MainViewModel이 카테고리 체크 후 필요시 DataStore 업데이트
        // PolaNavHost의 LaunchedEffect가 자동으로 네비게이션 처리
    }
}

/**
 * Home 탭 네비게이션 그래프
 */
@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.homeTabGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Home.route,
        route = NavGraphs.HOME_TAB
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToContents = { contentId ->
                    navController.navigate(Screen.Contents.createRoute(contentId))
                },
                onNavigateToCategory = { categoryId ->
                    navController.navigate(Screen.Category.createRoute(categoryId))
                },
                onNavigateToFavorite = {
                    navController.navigate(Screen.Favorite.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.SearchScreen.createRoute())
                },
                onNavigateToChatbot = {
                    navController.navigate(Screen.Chatbot.route)
                }
            )
        }

        composable(
            route = Screen.SearchScreen.route,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("tab") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val initialQuery = backStackEntry.arguments?.getString("query") ?: ""
            val initialTab = backStackEntry.arguments?.getString("tab") ?: ""

            SearchScreen(
                initialQuery = initialQuery,
                initialTab = initialTab,
                onBackClick = { navController.popBackStack() },
                onTagClick = { tagName ->
                    navController.navigate(Screen.Tag.createRoute(tagName.removePrefix("#"), "tag"))
                },
                onSearchClick = { searchQuery, searchType ->
                    navController.navigate(Screen.Tag.createRoute(searchQuery, searchType))
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
                navArgument("categoryId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: -1L
            CategoryScreen(
                navController = navController,
                categoryId = categoryId,
                onBackClick = { navController.popBackStack() },
                onNavigateToFavorite = {
                    navController.navigate(Screen.Favorite.route)
                },
                onNavigateToContents = { contentId, imageUrl ->
                    navController.navigate(Screen.Contents.createRoute(contentId, imageUrl))
                }
            )
        }

        composable(
            route = Screen.Tag.route,
            arguments = listOf(
                navArgument("tagName") { type = NavType.StringType },
                navArgument("searchType") {
                    type = NavType.StringType
                    defaultValue = "tag"
                }
            )
        ) { backStackEntry ->
            val tagName = backStackEntry.arguments?.getString("tagName") ?: ""
            val searchType = backStackEntry.arguments?.getString("searchType") ?: "tag"
            TagScreen(
                tagName = tagName,
                searchType = searchType,
                onBackClick = {
                    navController.popBackStack()
                },
                onSearchBarClick = {
                    navController.navigate(
                        Screen.SearchScreen.createRoute(
                            query = tagName,
                            tab = searchType
                        )
                    ) {
                        popUpTo(Screen.SearchScreen.route) { inclusive = true }
                    }
                },
                onNavigateToContents = { contentId ->
                    navController.navigate(Screen.Contents.createRoute(contentId))
                }
            )
        }

        composable(
            route = Screen.Contents.route,
            arguments = listOf(
                navArgument("contentId") { type = NavType.LongType },
                navArgument("imageUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getLong("contentId") ?: -1L
            val previewImageUrl = backStackEntry.arguments?.getString("imageUrl")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            }
            ContentsScreen(
                navController = navController,
                fileId = contentId,
                previewImageUrl = previewImageUrl,
                onBackClick = { navController.popBackStack() },
                onShareClick = { /* 내부 공유 기능 구현 */ },
                onEditClick = {
                    navController.navigate(Screen.ContentsEdit.createRoute(contentId))
                },
                onDeleteClick = { /* 내부 삭제 구현 */ },
                onTagClick = { tagName ->
                    navController.navigate(Screen.Tag.createRoute(tagName, "tag"))
                }
            )
        }

        composable(
            route = Screen.ContentsEdit.route,
            arguments = listOf(
                navArgument("contentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getLong("contentId") ?: -1L
            ContentsEditScreen(
                contentId = contentId,
                onBackClick = { navController.popBackStack() },
                onSaveClick = {
                    // 저장 성공 시 이전 화면으로 돌아감
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Favorite.route) {
            FavoriteScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToContents = { contentId ->
                    navController.navigate(Screen.Contents.createRoute(contentId))
                }
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
                onNavigateToContents = { contentId ->
                    navController.navigate(Screen.Contents.createRoute(contentId))
                },
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
                onNavigateToFavorite = {
                    navController.navigate(Screen.Favorite.route)
                },
                onNavigateToMyType = {
                    navController.navigate(Screen.MyType.route)
                },
                onNavigateToEditCategory = {
                    navController.navigate(Screen.EditCategory.route)
                },
                onNavigateToTermsOfService = {
                    navController.navigate(Screen.TermsOfService.route)
                },
            )
        }

        composable(Screen.MyType.route) {
            MyTypeScreen(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.EditCategory.route) {
            // MY_TAB 네비게이션 그래프 스코프의 공유 ViewModel 사용
            val myTabBackStackEntry = remember(it) {
                navController.getBackStackEntry(NavGraphs.MY_TAB)
            }
            val sharedCategoryViewModel: EditCategoryViewModel = hiltViewModel(myTabBackStackEntry)

            // EditTagScreen에서 뒤로가기한 경우가 아니면 항상 서버에서 최신 데이터 로드
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            LaunchedEffect(Unit) {
                if (previousRoute != Screen.EditTag.route) {
                    sharedCategoryViewModel.loadUserCategoriesWithTags()
                }
            }

            EditCategoryScreen(
                viewModel = sharedCategoryViewModel,
                onEditComplete = { categoriesWithTags ->
                    // DB 전체 상태 (초기 상태 비교용)
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "initialCategoriesWithTags",
                        sharedCategoryViewModel.getAllCategoriesWithTags()
                    )
                    // 선택된 카테고리 정보 (UI 표시용)
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "selectedCategoriesWithTags",
                        categoriesWithTags
                    )
                    // 카테고리 ID 맵
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "categoryIdMap",
                        sharedCategoryViewModel.getCategoryIdMap()
                    )
                    // 태그 편집 화면으로 이동
                    navController.navigate(Screen.EditTag.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditTag.route) {
            // MY_TAB 그래프 스코프의 ViewModel 사용 (상태 유지)
            val myTabBackStackEntry = remember(it) {
                navController.getBackStackEntry(NavGraphs.MY_TAB)
            }
            val sharedTagViewModel: EditTagViewModel = hiltViewModel(myTabBackStackEntry)
            val sharedCategoryViewModel: EditCategoryViewModel = hiltViewModel(myTabBackStackEntry)

            // DB에서 로드된 전체 카테고리 정보 (초기 상태 비교용)
            val initialCategoriesWithTags = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Map<String, List<Tag>>>("initialCategoriesWithTags")
                ?: emptyMap()

            // 선택된 카테고리 정보 (UI 표시용)
            val selectedCategoriesWithTags = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Map<String, List<Tag>>>("selectedCategoriesWithTags")
                ?: emptyMap()

            // 카테고리 ID 맵
            val categoryIdMap = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Map<String, Long>>("categoryIdMap")
                ?: emptyMap()

            // 초기 상태 설정
            LaunchedEffect(initialCategoriesWithTags, selectedCategoriesWithTags, categoryIdMap) {
                if (initialCategoriesWithTags.isNotEmpty()) {
                    sharedTagViewModel.initializeState(initialCategoriesWithTags, categoryIdMap)
                }
            }

            // Success 상태 감지 및 상태 초기화 후 MyScreen으로 이동
            val uiState by sharedTagViewModel.uiState.collectAsState()
            LaunchedEffect(uiState) {
                if (uiState is EditTagUiState.Success) {
                    // 두 ViewModel의 SavedStateHandle 모두 초기화
                    sharedTagViewModel.clearSavedState()
                    sharedCategoryViewModel.clearSavedState()

                    // MyScreen으로 직접 이동 (EditCategoryScreen으로 돌아가지 않음)
                    navController.navigate(Screen.My.route) {
                        popUpTo(Screen.My.route) { inclusive = false }
                    }
                }
            }

            EditTagScreen(
                categoriesWithTags = selectedCategoriesWithTags,
                viewModel = sharedTagViewModel,
                onEditComplete = { categoriesWithAllTags ->
                    // 변경사항 제출
                    sharedTagViewModel.submitChanges(categoriesWithAllTags)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TermsOfService.route) {
            TermsOfServiceScreen(

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
fun NavGraphBuilder.remindScreen(navController: NavHostController) {
    composable(Screen.Remind.route) {
        RemindScreen(
            onNavigateToContents = { contentId ->
                navController.navigate(Screen.Contents.createRoute(contentId))
            },
        )
    }
}