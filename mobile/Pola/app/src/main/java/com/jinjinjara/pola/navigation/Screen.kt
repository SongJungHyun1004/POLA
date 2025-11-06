package com.jinjinjara.pola.navigation

/**
 * 앱의 모든 화면 라우트를 정의하는 sealed class
 */
sealed class Screen(val route: String) {
    // Auth
    data object Start : Screen("start")
    data object CategorySelect : Screen("category_select")
    data object TagSelect : Screen("tag_select")

    // Main
    data object Home : Screen("home")
    data object Timeline : Screen("timeline")
    data object Upload : Screen("upload")
    data object Remind : Screen("remind")
    data object My : Screen("my")


    // Home 탭 내부 화면들
    data object HomeDetail : Screen("home/detail/{itemId}") {
        fun createRoute(itemId: String) = "home/detail/$itemId"
    }
    data object Category : Screen("category/{categoryName}") {
        fun createRoute(categoryName: String) = "category/$categoryName"
    }
    data object Tag : Screen("tag/{tagName}") {
        fun createRoute(tagName: String) = "tag/$tagName"
    }
    data object Contents : Screen("contents/{contentId}") {
        fun createRoute(contentId: String) = "contents/$contentId"
    }
    data object ContentsEdit : Screen("contents/edit/{contentId}") {
        fun createRoute(contentId: String) = "contents/edit/$contentId"
    }
    data object Favorite : Screen("favorite")
    data object SearchScreen : Screen("search_screen")
    data object Chatbot : Screen("chatbot")

    // Timeline 탭 내부 화면들
    data object TimelineDetail : Screen("timeline/detail/{postId}") {
        fun createRoute(postId: String) = "timeline/detail/$postId"
    }

    // My 탭 내부 화면들
    data object Profile : Screen("my/profile")
    data object Settings : Screen("my/settings")
    data object EditProfile : Screen("my/profile/edit")

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

    // 각 탭의 네비게이션 그래프
    const val HOME_TAB = "home_tab_graph"
    const val TIMELINE_TAB = "timeline_tab_graph"
    const val MY_TAB = "my_tab_graph"
}