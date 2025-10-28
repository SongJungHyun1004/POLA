package com.jinjinjara.pola.navigation

/**
 * 앱의 모든 화면 라우트를 정의하는 sealed class
 */
sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")
    data object SignUp : Screen("signup")

    // Main
    data object Home : Screen("home")
    data object Timeline : Screen("timeline")
    data object Upload : Screen("upload")
    data object Remind : Screen("remind")
    data object My : Screen("my")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")

    // 인자를 받는 화면 예시
    data object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }

    // 선택적 인자를 받는 화면 예시
    data object Search : Screen("search?query={query}") {
        fun createRoute(query: String? = null) =
            if (query != null) "search?query=$query"
            else "search"
    }
}

/**
 * 네비게이션 그래프 이름 정의
 */
object NavGraphs {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}