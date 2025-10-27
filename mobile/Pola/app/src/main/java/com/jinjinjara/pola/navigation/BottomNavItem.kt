package com.jinjinjara.pola.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom Navigation 아이템 정의
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "홈",
        icon = Icons.Default.Home
    )

    data object Timeline : BottomNavItem(
        route = Screen.Timeline.route,
        title = "타임라인",
        icon = Icons.Default.Search
    )

    data object Upload : BottomNavItem(
        route = Screen.Upload.route,
        title = "업로드",
        icon = Icons.Default.Star
    )

    data object Remind : BottomNavItem(
        route = Screen.Remind.route,
        title = "리마인드",
        icon = Icons.Default.Person
    )

    data object My : BottomNavItem(
        route = Screen.My.route,
        title = "마이",
        icon = Icons.Default.Settings
    )

    companion object {
        val items = listOf(Home, Timeline, Upload, Remind, My)
    }
}