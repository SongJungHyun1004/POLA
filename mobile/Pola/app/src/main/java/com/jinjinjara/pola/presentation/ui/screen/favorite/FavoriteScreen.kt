package com.jinjinjara.pola.presentation.ui.screen.favorite

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.DisplayItem
import com.jinjinjara.pola.presentation.ui.component.ItemGrid2View
import com.jinjinjara.pola.presentation.ui.component.ItemGrid3View
import com.jinjinjara.pola.presentation.ui.component.ItemListView

data class FavoriteItem(
    override val id: String,
    override val type: String,
    override val imageRes: Int,
    override val imageUrl: String,
    override val tags: List<String>,
    override val description: String,
    override val isFavorite: Boolean
) : DisplayItem

enum class ViewMode {
    LIST, GRID_3, GRID_2
}

@Composable
fun FavoriteScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNavigateToContents: (Long) -> Unit = {},
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 화면 진입 시 데이터 새로고침
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    // 에러 토스트 처리
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    var searchText by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("최신순") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    // UI 상태에 따른 분기
    when (val state = uiState) {
        is FavoriteUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }
        is FavoriteUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadFavorites() }) {
                        Text("다시 시도")
                    }
                }
            }
            return
        }
        is FavoriteUiState.Success -> {
            val favoriteItems = state.data

            // Empty 상태 처리
            if (favoriteItems.isEmpty()) {
                Scaffold(
                    topBar = {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .size(48.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onBackClick()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "닫기",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Text(
                                    text = "즐겨찾기",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(R.drawable.empty),
                                contentDescription = "No content",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "즐겨찾기 한 항목이 없습니다",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                return
            }

            FavoriteScreenContent(
                modifier = modifier,
                favoriteItems = favoriteItems,
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                isMenuExpanded = isMenuExpanded,
                onMenuExpandedChange = { isMenuExpanded = it },
                selectedSort = selectedSort,
                onSelectedSortChange = { selectedSort = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                onBackClick = onBackClick,
                onItemClick = { item ->
                    onNavigateToContents(item.fileId)
                },
                onFavoriteToggle = { item ->
                    viewModel.toggleFavorite(item.fileId)
                }
            )
        }
    }
}

@Composable
private fun FavoriteScreenContent(
    modifier: Modifier = Modifier,
    favoriteItems: List<com.jinjinjara.pola.domain.model.FavoriteData>,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    isMenuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    selectedSort: String,
    onSelectedSortChange: (String) -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    onBackClick: () -> Unit,
    onItemClick: (com.jinjinjara.pola.domain.model.FavoriteData) -> Unit,
    onFavoriteToggle: (com.jinjinjara.pola.domain.model.FavoriteData) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        /** 상단 앱바 */
        Surface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onBackClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "즐겨찾기",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        /** 검색창 */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            color = MaterialTheme.colorScheme.tertiary,
                            width = 2.dp
                        ),
                        color = Color.White,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 16.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchText.isEmpty()) {
                                    Text(
                                        text = "즐겨찾기의 태그를 검색해 보세요.",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }

                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "search",
                                modifier = Modifier
                                    .width(25.dp)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            )
        }

        /** 그리드 아이콘 및 정렬 메뉴 */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                        onViewModeChange(when (viewMode) {
                            ViewMode.LIST -> ViewMode.GRID_3
                            ViewMode.GRID_3 -> ViewMode.GRID_2
                            ViewMode.GRID_2 -> ViewMode.LIST
                        })
                    }
            )

            Box {
                Row(
                    modifier = Modifier.clickable { onMenuExpandedChange(true) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedSort,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "정렬",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) },
                    modifier = Modifier.background(Color.White)
                ) {
                    listOf("최신순", "오래된순").forEach { sort ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sort, color = MaterialTheme.colorScheme.tertiary)
                                    if (sort == selectedSort) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectedSortChange(sort)
                                onMenuExpandedChange(false)
                            }
                        )
                    }
                }
            }
        }

        /** 검색어로 필터링된 아이템 리스트 */
        val filteredItems = remember(searchText, favoriteItems) {
            if (searchText.isEmpty()) {
                favoriteItems
            } else {
                favoriteItems.filter { item ->
                    item.tags.any { tag ->
                        tag.contains(searchText, ignoreCase = true)
                    }
                }
            }
        }

        when (viewMode) {
            ViewMode.LIST -> {
                ItemListView(
                    items = filteredItems,
                    onFavoriteToggle = onFavoriteToggle,
                    onItemClick = onItemClick
                )
            }
            ViewMode.GRID_3 -> {
                ItemGrid3View(
                    items = filteredItems,
                    onFavoriteToggle = onFavoriteToggle,
                    onItemClick = onItemClick
                )
            }
            ViewMode.GRID_2 -> {
                ItemGrid2View(
                    items = filteredItems,
                    onFavoriteToggle = onFavoriteToggle,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoriteScreenPreview() {
    MaterialTheme {
        FavoriteScreen()
    }
}