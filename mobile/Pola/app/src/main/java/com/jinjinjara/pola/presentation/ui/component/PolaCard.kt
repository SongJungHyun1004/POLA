package com.jinjinjara.pola.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R

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
    backgroundColor: Color = Color.White,
    imageResId: Int? = null,
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
    content: @Composable (() -> Unit)? = null
) {
    // 전체 폴라로이드 카드
    Card(
        modifier = modifier
            .aspectRatio(ratio)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                RoundedCornerShape(5.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(5.dp)
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
                    .aspectRatio(imageRatio)
                    .clip(RoundedCornerShape(5.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                val painter = painterResource(
                    id = imageResId ?: R.drawable.temp_image
                )
                Image(
                    painter = painter,
                    contentDescription = "Content",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(5.dp)),
                    contentScale = ContentScale.Crop
                )
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