package com.jinjinjara.pola.navigation

import androidx.annotation.DrawableRes
import com.jinjinjara.pola.R

/**
 * Bottom Navigation 아이템 정의
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "홈",
        icon = R.drawable.home,
        selectedIcon = R.drawable.home_select
    )

    data object Timeline : BottomNavItem(
        route = Screen.Timeline.route,
        title = "타임라인",
        icon = R.drawable.timeline,
        selectedIcon = R.drawable.timeline_select
    )

    data object Upload : BottomNavItem(
        route = Screen.Upload.route,
        title = "업로드",
        icon = R.drawable.upload,
        selectedIcon = R.drawable.upload_select
    )

    data object Remind : BottomNavItem(
        route = Screen.Remind.route,
        title = "리마인드",
        icon = R.drawable.remind,
        selectedIcon = R.drawable.remind_select
    )

    data object My : BottomNavItem(
        route = Screen.My.route,
        title = "마이",
        icon = R.drawable.my,
        selectedIcon = R.drawable.my_select
    )

    companion object {
        val items by lazy { listOf(Home, Timeline, Upload, Remind, My) }
    }
}