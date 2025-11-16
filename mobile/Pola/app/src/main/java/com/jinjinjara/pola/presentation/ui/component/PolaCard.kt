package com.jinjinjara.pola.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.jinjinjara.pola.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun ClippedTagRowForCard(
    tags: List<String>,
    fontSize: TextUnit,
    color: Color,
    spacing: Dp = 4.dp,
    modifier: Modifier = Modifier
) {
    Layout(
        modifier = modifier,
        content = {
            tags.forEach { tag ->
                Text(
                    text = "#$tag",
                    fontSize = fontSize,
                    color = color,
                    maxLines = 1
                )
            }
        }
    ) { measurables, constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
        val placeables = measurables.map { it.measure(looseConstraints) }
        val spacingPx = spacing.roundToPx()

        var currentX = 0
        val visiblePlaceables = mutableListOf<androidx.compose.ui.layout.Placeable>()

        for (i in placeables.indices) {
            val placeable = placeables[i]
            val spaceNeeded = if (i > 0) spacingPx else 0

            if (currentX + spaceNeeded + placeable.width <= constraints.maxWidth) {
                visiblePlaceables.add(placeable)
                currentX += spaceNeeded + placeable.width
            } else {
                break
            }
        }

        val height = visiblePlaceables.maxOfOrNull { it.height } ?: 0

        layout(constraints.maxWidth, height) {
            var x = 0
            visiblePlaceables.forEachIndexed { index, placeable ->
                if (index > 0) x += spacingPx
                placeable.placeRelative(x, 0)
                x += placeable.width
            }
        }
    }
}

@Composable
fun PolaCard(
    modifier: Modifier = Modifier,
    contentAlpha: Float? = null,
    backgroundColor: Color = Color.White,
    imageResId: Int? = null,
    imageUrl: String? = null,
    placeholderImageUrl: String? = null,
    type: String? = "image",
    ratio: Float = 3f / 4f,
    imageRatio: Float = 1f,
    paddingValues: PaddingValues = PaddingValues(
        top = 32.dp,
        start = 32.dp,
        end = 32.dp
    ),
    borderColor: Color = MaterialTheme.colorScheme.tertiary,
    textList: List<String> = emptyList(),
    textSize: TextUnit = 24.sp,
    textSpacing: Dp = 8.dp,
    clipTags: Boolean = false,
    timeAgo: String? = null,
    sourceIcon: Int? = null,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    // 전체 폴라로이드 카드
    Card(
        shape = RoundedCornerShape(5.dp),
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(5.dp),
                clip = false
            )
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(5.dp),
                clip = false
            )
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(5.dp),
                clip = false
            )
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(5.dp),
                clip = false
            )
            .aspectRatio(ratio),
//            .border(
//                BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
//                RoundedCornerShape(5.dp)
//            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),

    ) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            // 내부 사진 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { base ->
                        if (contentAlpha != null) {
                            base.alpha(contentAlpha)
                        } else {
                            base // alpha 적용 안함
                        }
                    }
                    .aspectRatio(imageRatio)
                    .clip(RoundedCornerShape(5.dp))
                    .drawWithContent {
                        drawContent()

                        // 인너섀도우
                        val shadowColor = Color.Black.copy(alpha = 0.2f)  // 반투명 검정
                        val cornerRadius = 5.dp.toPx()
                        val shadowSize = 4.dp.toPx()

                        // 네 방향 그라데이션
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(shadowColor, Color.Transparent),
                                startY = 0f,
                                endY = shadowSize
                            )
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, shadowColor),
                                startY = size.height - shadowSize,
                                endY = size.height
                            )
                        )
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(shadowColor, Color.Transparent),
                                startX = 0f,
                                endX = shadowSize
                            )
                        )
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, shadowColor),
                                startX = size.width - shadowSize,
                                endX = size.width
                            )
                        )
                    },
//                    .border(1.dp, borderColor, RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    // 이미지 파일
                    type?.startsWith("image") == true && imageUrl != null -> {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Image Content",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = if (!placeholderImageUrl.isNullOrEmpty()) {
                                rememberAsyncImagePainter(placeholderImageUrl)
                            } else {
                                null
                            },
                        )
                    }

                    // 텍스트 파일
                    type?.startsWith("text") == true && imageUrl != null -> {
                        var textContent by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(imageUrl) {
                            try {
                                textContent = withContext(Dispatchers.IO) {
                                    URL(imageUrl).readText(Charsets.UTF_8)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                textContent = "(텍스트 로드 실패)"
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF5F5F5))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = textContent ?: "로딩 중...",
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 기본 이미지 (빈 경우)
                    else -> {
                        val painter = painterResource(
                            id = imageResId ?: R.drawable.temp_image
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Default Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // 메타 정보 영역
            if (timeAgo != null || sourceIcon != null || isFavorite) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (timeAgo != null) {
                            Text(
                                text = timeAgo,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        if (sourceIcon != null) {
                            if (timeAgo != null) {
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                )
                            }
                            Icon(
                                painter = painterResource(id = sourceIcon),
                                contentDescription = "출처",
                                modifier = Modifier.size(16.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }

                    Icon(
                        painter = painterResource(
                            id = if (isFavorite) R.drawable.star_primary_solid
                            else R.drawable.star_primary
                        ),
                        contentDescription = "즐겨찾기",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onFavoriteClick?.invoke()
                            }
                    )
                }
            }

            // 정보 영역
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (clipTags) {
                    ClippedTagRowForCard(
                        tags = textList,
                        fontSize = textSize,
                        color = MaterialTheme.colorScheme.tertiary,
                        spacing = textSpacing,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(textSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        textList.forEach { text ->
                            Text(
                                text = "#$text",
                                fontSize = textSize,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PolaCardPreview() {
    PolaCard() {
        // 미리보기용 빈 콘텐츠
    }
}