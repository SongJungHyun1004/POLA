package com.jinjinjara.pola.presentation.ui.screen.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import com.jinjinjara.pola.presentation.ui.component.PolaSearchBar
import com.jinjinjara.pola.presentation.ui.screen.timeline.CategoryChips
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt
import androidx.compose.ui.zIndex
import com.jinjinjara.pola.presentation.ui.component.DisplayItem
import com.jinjinjara.pola.presentation.ui.component.ItemGrid2View
import com.jinjinjara.pola.presentation.ui.component.ItemGrid3View


data class CategoryItem(
    override val id: String,
    val name: String,
    override val imageRes: Int = R.drawable.temp_image,
    override val imageUrl: String = "",
    override val tags: List<String> = listOf(name),
    override val description: String = "",
    override val isFavorite: Boolean = false
) : DisplayItem

enum class ViewMode {
    GRID_3, GRID_2
}

@Composable
fun CategoryScreen(
    categoryName: String = "카테고리",
    onBackClick: () -> Unit = {},
    onNavigateToContents : (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("전체") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("최신순") }
    var viewMode by remember { mutableStateOf(ViewMode.GRID_3) }

    val categories = listOf(
        CategoryItem("1", "말차"),
        CategoryItem("2", "초코"),
        CategoryItem("3", "딸기"),
        CategoryItem("1", "말차"),
        CategoryItem("2", "초코"),
        CategoryItem("3", "딸기"),
        CategoryItem("1", "말차"),
        CategoryItem("2", "초코"),
        CategoryItem("3", "딸기"),
        CategoryItem("1", "말차"),
        CategoryItem("2", "초코"),
        CategoryItem("3", "딸기"),
        CategoryItem("1", "말차"),
        CategoryItem("2", "초코"),
        CategoryItem("3", "딸기"),
        CategoryItem("1", "말차"),
        CategoryItem("2", "초코"),
        CategoryItem("3", "딸기"),
        CategoryItem("1", "말차"),
        CategoryItem("2", "초코"),
        CategoryItem("3", "딸기"),
        CategoryItem("1", "말차"),
        CategoryItem("2", "초코"),
        CategoryItem("3", "딸기"),
    )

    var searchText by remember { mutableStateOf("") }

    val gridState = rememberLazyGridState()

    // 헤더 높이
    val headerHeightDp = 220.dp
    val density = LocalDensity.current
    val headerHeightPx = with(density) { headerHeightDp.roundToPx() }
    val toolbarOffset = remember { mutableStateOf(0f) }

    // 스크롤 델타 추적: snapshotFlow로 firstVisibleItemScrollOffset + index를 합쳐 연속 변화량 계산
    LaunchedEffect(gridState) {
        var previous = Pair(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset)
        snapshotFlow {
            Pair(
                gridState.firstVisibleItemIndex,
                gridState.firstVisibleItemScrollOffset
            )
        }
            .map { current ->
                // compute dy in px: positive when scrolling down (content moves up)
                val (prevIndex, prevOffset) = previous
                val (curIndex, curOffset) = current
                val indexDiff = (curIndex - prevIndex)
                val dy = if (indexDiff == 0) {
                    (curOffset - prevOffset)
                } else {
                    // if index changed, approximate dy by big jump -> set dy sign
                    // when index increased -> big positive scroll (down)
                    if (indexDiff > 0) headerHeightPx.toFloat() else -headerHeightPx.toFloat()
                }
                previous = current
                dy.toFloat()
            }
            .distinctUntilChanged()
            .collectLatest { dy ->
                // dy > 0 : user scrolled DOWN (content moves up) -> hide header (increase offset)
                // dy < 0 : user scrolled UP (content moves down) -> show header (decrease offset)
                if (dy > 0f) {
                    // scroll down: gradually hide header
                    toolbarOffset.value =
                        (toolbarOffset.value + dy).coerceAtMost(headerHeightPx.toFloat())
                } else if (dy < 0f) {
                    // quick show on any small upward scroll:
                    // if the user scrolls up at all, reveal header immediately (YouTube-like)
                    toolbarOffset.value = 0f
                }
            }
    }

    // animated offset for smooth movement
    val animOffset by animateFloatAsState(
        targetValue = toolbarOffset.value,
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (viewMode) {
            ViewMode.GRID_3 -> {
                ItemGrid3View(
                    items = categories,
                    onItemClick = { item ->
                        onNavigateToContents(item.id)
                    },
                    onFavoriteToggle = { }, // 빈 람다 (기능 없음)
                    state = gridState,
                    contentPadding = PaddingValues(
                        top = headerHeightDp + 8.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    showFavoriteIcon = false,
                    modifier = Modifier.fillMaxSize()
                )
            }
            ViewMode.GRID_2 -> {
                ItemGrid2View(
                    items = categories,
                    onItemClick = { item ->
                        onNavigateToContents(item.id)
                    },
                    onFavoriteToggle = { }, // 빈 람다 (기능 없음)
                    state = gridState,
                    contentPadding = PaddingValues(
                        top = headerHeightDp + 8.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    showFavoriteIcon = false,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        // Category Grid
//        LazyVerticalGrid(
//            state = gridState,
//            columns = GridCells.Fixed(3),
//            contentPadding = PaddingValues(
//                top = headerHeightDp + 8.dp,
//                start = 16.dp,
//                end = 16.dp,
//                bottom = 16.dp
//            ),
//            horizontalArrangement = Arrangement.spacedBy(12.dp),
//            verticalArrangement = Arrangement.spacedBy(24.dp),
//            modifier = Modifier.fillMaxSize()
//        ) {
//            items(categories) { category ->
//                PolaCard(
//                    modifier = Modifier.shadow(elevation = 8.dp),
//                    ratio = 0.7661f,
//                    imageRatio = 0.9062f,
//                    paddingValues = PaddingValues(top = 4.dp, start = 4.dp, end = 4.dp),
//                    imageResId = R.drawable.temp_image,
//                    textList = listOf(category.name),
//                    textSize = 12.sp,
//                    textSpacing = 8.dp,
//                )
//            }
//        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, (-animOffset).roundToInt()) }
                .zIndex(1f)
        ) {
            // 배경만 담당하는 Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeightDp)
                    .background(MaterialTheme.colorScheme.background)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (animOffset >= headerHeightPx * 0.99f) 0f else 1f)
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "뒤로가기",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onBackClick()
                            }
                    )

                    Text(
                        text = categoryName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Icon(
                        painter = painterResource(R.drawable.star),
                        contentDescription = "Favorites",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // 즐겨찾기 이동
                            }
                            .size(30.dp)
                    )
                }
                // Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PolaSearchBar(
                        searchText = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // CategoryChips
                CategoryChips(
                    categories = listOf("전체", "말차", "초코", "딸기"),
                    selectedCategory = selectedTab,
                    onCategorySelected = { selectedTab = it }
                )

                // Grid Icon and Sort Menu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (viewMode == ViewMode.GRID_3) R.drawable.grid_3 else R.drawable.gird_2
                        ),
                        contentDescription = "뷰 모드 변경",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewMode = if (viewMode == ViewMode.GRID_3) ViewMode.GRID_2 else ViewMode.GRID_3
                            }
                    )
                    Box {
                        Row(
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isMenuExpanded = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedSort,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector =
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = "정렬",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (isMenuExpanded) {
                            Popup(
                                alignment = Alignment.TopEnd,
                                onDismissRequest = { isMenuExpanded = false },
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .shadow(12.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .padding(vertical = 8.dp)
                                ) {
                                    // 상단 제목
                                    Text(
                                        text = "정렬",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )

                                    val sortOptions = listOf("태그순", "최신순", "오래된순")
                                    sortOptions.forEachIndexed { index, sort ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedSort = sort
                                                    isMenuExpanded = false
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sort,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontSize = 12.sp
                                            )
                                            if (sort == selectedSort) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    CategoryScreen()
}