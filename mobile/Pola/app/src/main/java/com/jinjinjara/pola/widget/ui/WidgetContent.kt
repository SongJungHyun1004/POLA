package com.jinjinjara.pola.widget.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jinjinjara.pola.R
import com.jinjinjara.pola.widget.callback.RefreshCallback
import com.jinjinjara.pola.widget.data.WidgetState

/**
 * 위젯 콘텐츠 레이아웃
 */
@Composable
fun WidgetContentLayout(
    widgetState: WidgetState,
    currentBitmap: Bitmap?,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .background(ImageProvider(R.drawable.widget_preview)),
        contentAlignment = Alignment.Center
    ) {
        when {
            widgetState.isLoading -> {
                LoadingContent()
            }

            widgetState.remindItems.isNotEmpty() -> {
                // 캐시된 데이터가 있으면 데이터 표시 (오류가 있어도)
                RemindContent(widgetState, currentBitmap)
            }

            widgetState.errorMessage != null -> {
                // 캐시된 데이터가 없고 오류가 있으면 오류 화면 표시
                ErrorContent(message = widgetState.errorMessage)
            }

            else -> {
                // 데이터도 없고 오류도 없으면 빈 화면
                EmptyContent()
            }
        }
    }
}

/**
 * 로딩 상태 UI
 */
@Composable
private fun LoadingContent() {
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "로딩 중...",
            style = TextStyle(color = GlanceTheme.colors.onBackground)
        )
    }
}

/**
 * 에러 상태 UI
 */
@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "오류 발생",
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = message,
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(16.dp))

        // 새로고침 버튼
        Image(
            provider = ImageProvider(R.drawable.ic_refresh),
            contentDescription = "새로고침",
            modifier = GlanceModifier
                .size(40.dp)
                .clickable(onClick = actionRunCallback<RefreshCallback>())
        )
    }
}

/**
 * 데이터 없음 UI
 */
@Composable
private fun EmptyContent() {
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "표시할 리마인드가 없습니다",
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = GlanceModifier.height(16.dp))

        // 새로고침 버튼
        Image(
            provider = ImageProvider(R.drawable.ic_refresh),
            contentDescription = "새로고침",
            modifier = GlanceModifier
                .size(40.dp)
                .clickable(onClick = actionRunCallback<RefreshCallback>())
        )
    }
}

/**
 * 실제 리마인드 UI (이미지 + 인덱스 + 버튼 3개)
 */
@Composable
private fun RemindContent(widgetState: WidgetState, currentBitmap: Bitmap?) {
    val currentItem = widgetState.remindItems.getOrNull(widgetState.currentIndex)

    if (currentItem != null) {

        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {

            // 1) 이미지 영역
            WidgetPolaCardContent(
                bitmap = currentBitmap,
                tags = currentItem.tags,
                isFavorite = currentItem.isFavorite,
                fileId = currentItem.fileId,
                modifier = GlanceModifier.fillMaxWidth()
            )


            Spacer(GlanceModifier.height(6.dp))

            // 2) 인덱스 표시 (2 / 5)
            Text(
                text = "${widgetState.currentIndex + 1} / ${widgetState.remindItems.size}",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF999999)),  // 회색
                    fontSize = 14.sp,
                )
            )

            Spacer(GlanceModifier.height(12.dp))

            // 3) ← ★ → 버튼
            WidgetControlsContent(
                isFavorite = currentItem.isFavorite,
                modifier = GlanceModifier.fillMaxWidth()
            )

            // 4) 오류 메시지가 있으면 작게 표시 (캐시된 데이터 사용 중)
            if (widgetState.errorMessage != null) {
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    text = "⚠ ${widgetState.errorMessage}",
                    style = TextStyle(
                        color = GlanceTheme.colors.error,
                        fontSize = 12.sp
                    )
                )
            }
        }
    } else {
        EmptyContent()
    }
}
