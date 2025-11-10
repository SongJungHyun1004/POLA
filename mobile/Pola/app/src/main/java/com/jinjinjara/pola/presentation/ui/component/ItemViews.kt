package com.jinjinjara.pola.presentation.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R

/** 공통 아이템 인터페이스 */
interface DisplayItem {
    val id: String
    val imageRes: Int
    val imageUrl: String
    val tags: List<String>
    val description: String
    val isFavorite: Boolean
}

/** 태그가 잘리면 전체 숨김 처리하는 태그 Row */
@Composable
fun ClippedTagRow(
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
                    text = tag,
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

/** 리스트뷰 아이템 컴포넌트 */
@Composable
fun <T : DisplayItem> ItemListItem(
    item: T,
    onFavoriteToggle: (T) -> Unit,
    onItemClick: ((T) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = onItemClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onItemClick?.invoke(item)
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽: 1:1 이미지
        if (item.imageRes != 0) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        }

        // 중간: 태그와 설명
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 해시태그들
            ClippedTagRow(
                tags = item.tags.map { "#${it.removePrefix("#")}" },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.tertiary,
                spacing = 4.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // 설명
            Text(
                text = item.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 오른쪽: 즐겨찾기 토글 버튼
        Icon(
            painter = painterResource(
                id = if (item.isFavorite) R.drawable.star_primary_solid
                else R.drawable.star_primary
            ),
            contentDescription = "즐겨찾기",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(30.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onFavoriteToggle(item)
                }
        )
    }
}

/** 리스트뷰 컴포넌트 */
@Composable
fun <T : DisplayItem> ItemListView(
    items: List<T>,
    onFavoriteToggle: (T) -> Unit,
    onItemClick: ((T) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(items) { item ->
            ItemListItem(
                item = item,
                onFavoriteToggle = onFavoriteToggle,
                onItemClick = onItemClick
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        }
    }
}

/** 3열 그리드뷰 컴포넌트 */
@Composable
fun <T : DisplayItem> ItemGrid3View(
    items: List<T>,
    onFavoriteToggle: (T) -> Unit,
    onItemClick: ((T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showFavoriteIcon: Boolean = true
) {
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(3),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(items) { item ->
            Box(
                modifier = Modifier.clickable(
                    enabled = onItemClick != null,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onItemClick?.invoke(item)
                }
            ) {
                PolaCard(
                    modifier = Modifier.shadow(elevation = 8.dp),
                    ratio = 0.7661f,
                    imageRatio = 0.9062f,
                    paddingValues = PaddingValues(
                        top = 4.dp,
                        start = 4.dp,
                        end = 4.dp
                    ),
                    imageResId = if (item.imageRes != 0) item.imageRes else null,
                    imageUrl = if (item.imageRes == 0) item.imageUrl else null,
                    textList = item.tags.map { it.removePrefix("#") },
                    textSize = 12.sp,
                    textSpacing = 8.dp,
                    clipTags = true
                )

                // 즐겨찾기 토글 아이콘
                if (showFavoriteIcon) {
                    Icon(
                        painter = painterResource(
                            id = if (item.isFavorite) R.drawable.star_primary_solid
                            else R.drawable.star_primary
                        ),
                        contentDescription = "즐겨찾기",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(22.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onFavoriteToggle(item)
                            }
                    )
                }
            }
        }
    }
}

/** 2열 그리드뷰 컴포넌트 */
@Composable
fun <T : DisplayItem> ItemGrid2View(
    items: List<T>,
    onFavoriteToggle: (T) -> Unit,
    onItemClick: ((T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showFavoriteIcon: Boolean = true
) {
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(2),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(items) { item ->
            Box(
                modifier = Modifier.clickable(
                    enabled = onItemClick != null,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onItemClick?.invoke(item)
                }
            ) {
                PolaCard(
                    modifier = Modifier.shadow(elevation = 8.dp),
                    ratio = 0.7661f,
                    imageRatio = 0.9062f,
                    paddingValues = PaddingValues(
                        top = 8.dp,
                        start = 8.dp,
                        end = 8.dp
                    ),
                    imageResId = if (item.imageRes != 0) item.imageRes else null,
                    imageUrl = if (item.imageRes == 0) item.imageUrl else null,
                    textList = item.tags.map { it.removePrefix("#") },
                    textSize = 16.sp,
                    textSpacing = 8.dp,
                    clipTags = true
                )

                // 즐겨찾기 토글 아이콘
                if (showFavoriteIcon) {
                    Icon(
                        painter = painterResource(
                            id = if (item.isFavorite) R.drawable.star_primary_solid
                            else R.drawable.star_primary
                        ),
                        contentDescription = "즐겨찾기",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(28.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onFavoriteToggle(item)
                            }
                    )
                }
            }
        }
    }
}
