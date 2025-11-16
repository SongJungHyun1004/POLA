package com.jinjinjara.pola.widget.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jinjinjara.pola.R
import com.jinjinjara.pola.widget.callback.NavigateToContentCallback

/**
 * 위젯 폴라 카드 컴포넌트 - 기존 앱 스타일
 */
@Composable
fun WidgetPolaCardContent(
    bitmap: Bitmap?,
    tags: List<String>,
    isFavorite: Boolean,
    fileId: Long,
    modifier: GlanceModifier = GlanceModifier
) {
    // 카드 전체 (테두리 + 둥근 모서리)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(490.dp)
            .background(ImageProvider(R.drawable.widget_card_background))
            .padding(24.dp)  // 원본 32dp의 75%
            .clickable(
                onClick = actionRunCallback<NavigateToContentCallback>()
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.Top
        ) {
            // 1. 이미지 영역
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                // 먼저 이미지 렌더링
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = "Pola Image",
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                } else {
                    PlaceholderImage()
                }

                // 그 위에 테두리 오버레이
                Image(
                    provider = ImageProvider(R.drawable.widget_image_border),
                    contentDescription = "Image Border",
                    contentScale = ContentScale.FillBounds,
                    modifier = GlanceModifier.fillMaxSize()
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // 2. 태그 영역 (한 줄에 들어가는 만큼 - WidgetRepository에서 필터링됨)
            if (tags.isNotEmpty()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.Start,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = TextStyle(
                                fontSize = 28.sp,
                                color = ColorProvider(Color(0xFF4C3D25))
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * 플레이스홀더 이미지 컴포넌트
 */
@Composable
private fun PlaceholderImage() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_image_border)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFFF5F5F5))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "이미지 로딩 중...",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF999999)),
                    fontSize = 12.sp
                )
            )
        }
    }
}
