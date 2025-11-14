package com.jinjinjara.pola.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import com.jinjinjara.pola.R
import com.jinjinjara.pola.widget.callback.NavigatePreviousCallback
import com.jinjinjara.pola.widget.callback.NavigateNextCallback
import com.jinjinjara.pola.widget.callback.ToggleFavoriteCallback

/**
 * ←  ★  → 버튼만 표시하는 컨트롤 UI
 * 인덱스(2/5)는 WidgetContentLayout에서 별도로 표시함.
 */
@Composable
fun WidgetControlsContent(
    isFavorite: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {

        // ← 이전 버튼
        Image(
            provider = ImageProvider(R.drawable.arrow_left),
            contentDescription = "Previous",
            modifier = GlanceModifier
                .clickable(onClick = actionRunCallback<NavigatePreviousCallback>())
                .size(30.dp)
        )

        Spacer(GlanceModifier.width(48.dp))

        // ★ 즐겨찾기 버튼
        Image(
            provider = ImageProvider(
                if (isFavorite) R.drawable.star_primary_solid else R.drawable.star_primary
            ),
            contentDescription = "Favorite",
            modifier = GlanceModifier
                .clickable(onClick = actionRunCallback<ToggleFavoriteCallback>())
                .size(30.dp)
        )

        Spacer(GlanceModifier.width(48.dp))

        // → 다음 버튼
        Image(
            provider = ImageProvider(R.drawable.arrow_right),
            contentDescription = "Next",
            modifier = GlanceModifier
                .clickable(onClick = actionRunCallback<NavigateNextCallback>())
                .size(30.dp)
        )
    }
}
