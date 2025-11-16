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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
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
import com.jinjinjara.pola.presentation.ui.component.SearchBar
import com.jinjinjara.pola.navigation.Screen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jinjinjara.pola.presentation.ui.component.CategoryChips
import com.jinjinjara.pola.presentation.ui.component.DisplayItem
import com.jinjinjara.pola.presentation.ui.component.ItemGrid2View
import com.jinjinjara.pola.presentation.ui.component.ItemGrid3View
import com.jinjinjara.pola.presentation.ui.component.ItemListView
import com.jinjinjara.pola.domain.model.UserCategory


enum class ViewMode {
    LIST, GRID_3, GRID_2
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: Long = -1L,
    onBackClick: () -> Unit = {},
    onNavigateToFavorite: () -> Unit = {},
    onNavigateToContents : (Long, String?) -> Unit = { _, _ -> },
    navController: NavHostController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedCategoryId by remember { mutableStateOf(categoryId) }
    var selectedTab by remember {
        mutableStateOf(
            uiState.userCategories.find { it.id == categoryId }?.categoryName ?: "전체"
        )
    }

    // SavedStateHandle 값 감시
    val refreshNeeded = navController
        .currentBackStackEntryFlow
        .collectAsState(initial = null)

    LaunchedEffect(refreshNeeded.value) {
        // SavedStateHandle에 "refreshNeeded"가 true이면 갱신
        val refresh = navController
            .currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("refreshNeeded") ?: false

        if (refresh) {
            viewModel.refresh()

            // 다시 false로 초기화
            navController
                .currentBackStackEntry
                ?.savedStateHandle
                ?.set("refreshNeeded", false)
        }
    }

    // 디버깅 로그 추가
    LaunchedEffect(uiState.categoryName, uiState.userCategories) {
        if (selectedCategoryId != null) return@LaunchedEffect
        android.util.Log.d("CategoryScreen", "categoryName: ${uiState.categoryName}")
        android.util.Log.d("CategoryScreen", "userCategories: ${uiState.userCategories.map { it.categoryName }}")
        android.util.Log.d("CategoryScreen", "selectedTab: $selectedTab")
        if (uiState.userCategories.isNotEmpty()) {
            val currentCategory = uiState.userCategories.find { it.id == categoryId }
            selectedCategoryId = currentCategory?.id ?: -1
            selectedTab = currentCategory?.categoryName ?: "전체"
        }
    }

    // uiState.categoryName이 로드되면 selectedTab 업데이트
    LaunchedEffect(uiState.categoryName) {
        if (uiState.categoryName.isNotEmpty()) {
            android.util.Log.d("CategoryScreen", "Updating selectedTab to: ${uiState.categoryName}")
            selectedTab = uiState.categoryName
        }
    }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("최신순") }
    var viewMode by remember { mutableStateOf(ViewMode.GRID_2) }

    val categories = uiState.files

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    val showScrollToTopButton by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    // 헤더 높이
    val headerHeightDp = 160.dp
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

    // Pull-to-Refresh 상태 관리
    LaunchedEffect(uiState) {
        if (!uiState.isLoading) {
            isRefreshing = false
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
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeightDp + 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            uiState.files.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeightDp + 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.empty),
                            contentDescription = "Empty Content",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "이 카테고리에 분류된 컨텐츠가 없어요",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        viewModel.refresh()
                    },
                    state = pullRefreshState,
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            state = pullRefreshState,
                            isRefreshing = isRefreshing,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = 160.dp),
                            color = MaterialTheme.colorScheme.background,
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    when (viewMode) {
                        ViewMode.LIST -> {
                            ItemListView(
                                items = categories,
                                onItemClick = { item ->
                                    onNavigateToContents(item.fileId, item.imageUrl)
                                },
                                onFavoriteToggle = null, // 즐겨찾기 아이콘 숨김
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = headerHeightDp + 8.dp)
                            )
                        }

                        ViewMode.GRID_3 -> {
                            ItemGrid3View(
                                items = categories,
                                onItemClick = { item ->
                                    onNavigateToContents(item.fileId, item.imageUrl)
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
                                    onNavigateToContents(item.fileId, item.imageUrl)
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
                }
            }
        }

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
                        .padding(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        ),
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

                    Spacer(Modifier.width(12.dp))

                    SearchBar(
                        searchText = "",
                        onSearchClick = {
                            navController.navigate(Screen.SearchScreen.createRoute())
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(12.dp))

                    Icon(
                        painter = painterResource(R.drawable.star),
                        contentDescription = "Favorites",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onNavigateToFavorite()
                            }
                            .size(30.dp)
                    )
                }

                // CategoryChips
                val categories = remember(uiState.userCategories) {
                    val sortedCategories = uiState.userCategories.sortedWith(
                        compareByDescending<UserCategory> { it.fileCount }
                            .thenBy { if (it.categoryName == "미분류") 1 else 0 }
                            .thenBy { it.categoryName }
                    )
                    listOf(UserCategory(-1, "전체")) + sortedCategories
                }

                CategoryChips(
                    categories = categories.map { it.categoryName },
                    selectedCategory = categories.find { it.id == selectedCategoryId }?.categoryName ?: "전체",
                    onCategorySelected = { selectedName ->
                        val selectedCategory = categories.find { it.categoryName == selectedName }
                        selectedCategoryId = selectedCategory?.id ?: -1
                        selectedTab = selectedName
                        viewModel.selectCategory(selectedCategoryId)
                    }
                )

                // Category Title, Grid Icon and Sort Menu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Title
                    Text(
                        text = uiState.categoryName.ifEmpty { "전체" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    // Grid Toggle + Sort Menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = when (viewMode) {
                                    ViewMode.LIST -> R.drawable.list
                                    ViewMode.GRID_3 -> R.drawable.grid_3
                                    ViewMode.GRID_2 -> R.drawable.gird_2
                                }
                            ),
                            contentDescription = "뷰 모드 변경",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewMode = when (viewMode) {
                                        ViewMode.LIST -> ViewMode.GRID_3
                                        ViewMode.GRID_3 -> ViewMode.GRID_2
                                        ViewMode.GRID_2 -> ViewMode.LIST
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                                fontSize = 14.sp
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

                                    val sortOptions = listOf("최신순", "오래된순", "조회순")
                                    sortOptions.forEachIndexed { index, sort ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedSort = sort
                                                    isMenuExpanded = false
                                                    val (sortBy, direction) = when (sort) {
                                                        "최신순" -> "createdAt" to "DESC"
                                                        "오래된순" -> "createdAt" to "ASC"
                                                        "조회순" -> "views" to "DESC"
                                                        else -> "createdAt" to "DESC"
                                                    }

                                                    viewModel.updateSort(sortBy, direction)
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sort,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontSize = 14.sp
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

        // 맨 위로 버튼
        if (showScrollToTopButton) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        gridState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .size(36.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50),
                elevation = FloatingActionButtonDefaults.elevation(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "맨 위로",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

    }
}

//@Preview(showBackground = true)
//@Composable
//fun CategoryScreenPreview() {
//    CategoryScreen()
//}