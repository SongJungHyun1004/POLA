package com.jinjinjara.pola.presentation.ui.screen.favorite

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.DisplayItem
import com.jinjinjara.pola.presentation.ui.component.ItemGrid2View
import com.jinjinjara.pola.presentation.ui.component.ItemGrid3View
import com.jinjinjara.pola.presentation.ui.component.ItemListView

data class FavoriteItem(
    override val id: String,
    override val imageRes: Int,
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
    onBackClick: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("최신순") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    var favoriteItems by remember {
        mutableStateOf(
            listOf(
                FavoriteItem(
                    id = "1",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#카페", "#디저트", "#브런치", "#딸기", "#말차라떼", "#초코", "#커피"),
                    description = "분위기 좋은 카페에서 여유로운 시간 분위기 좋은 카페에서 여유로운 시간 분위기 좋은 카페에서 여유로운 시간 분위기 좋은 카페에서 여유로운 시간",
                    isFavorite = true
                ),
                FavoriteItem(
                    id = "2",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#음식", "#맛집", "#한식"),
                    description = "정갈한 한정식이 맛있는 곳",
                    isFavorite = true
                ),
                FavoriteItem(
                    id = "3",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#여행", "#풍경", "#힐링"),
                    description = "자연 속에서 힐링하기 좋은 장소",
                    isFavorite = true
                ),
                FavoriteItem(
                    id = "4",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#쇼핑", "#패션", "#트렌드"),
                    description = "최신 트렌드를 만날 수 있는 쇼핑몰",
                    isFavorite = true
                ),
                FavoriteItem(
                    id = "5",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#영화", "#데이트", "#주말"),
                    description = "주말에 가기 좋은 영화관",
                    isFavorite = false
                ),
                FavoriteItem(
                    id = "6",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#운동", "#헬스", "#건강"),
                    description = "시설 좋은 피트니스 센터",
                    isFavorite = true
                ),
                FavoriteItem(
                    id = "7",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#전시", "#미술관", "#문화"),
                    description = "감성 충만한 전시회 관람",
                    isFavorite = true
                ),
                FavoriteItem(
                    id = "8",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#반려동물", "#애견카페"),
                    description = "반려견과 함께 갈 수 있는 카페",
                    isFavorite = false
                ),
                FavoriteItem(
                    id = "9",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#야경", "#드라이브", "#야간"),
                    description = "멋진 야경을 감상할 수 있는 명소",
                    isFavorite = true
                ),
                FavoriteItem(
                    id = "10",
                    imageRes = R.drawable.temp_image,
                    tags = listOf("#공부", "#카페", "#조용"),
                    description = "집중하기 좋은 조용한 스터디 카페",
                    isFavorite = true
                ),
            )
        )
    }

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
                onValueChange = { searchText = it },
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
                        viewMode = when (viewMode) {
                            ViewMode.LIST -> ViewMode.GRID_3
                            ViewMode.GRID_3 -> ViewMode.GRID_2
                            ViewMode.GRID_2 -> ViewMode.LIST
                        }
                    }
            )

            Box {
                Row(
                    modifier = Modifier.clickable { isMenuExpanded = true },
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
                    onDismissRequest = { isMenuExpanded = false },
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
                                selectedSort = sort
                                isMenuExpanded = false
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
                    onFavoriteToggle = { item ->
                        favoriteItems = favoriteItems.map {
                            if (it.id == item.id) it.copy(isFavorite = !it.isFavorite)
                            else it
                        }
                    }
                )
            }
            ViewMode.GRID_3 -> {
                ItemGrid3View(
                    items = filteredItems,
                    onFavoriteToggle = { item ->
                        favoriteItems = favoriteItems.map {
                            if (it.id == item.id) it.copy(isFavorite = !it.isFavorite)
                            else it
                        }
                    }
                )
            }
            ViewMode.GRID_2 -> {
                ItemGrid2View(
                    items = filteredItems,
                    onFavoriteToggle = { item ->
                        favoriteItems = favoriteItems.map {
                            if (it.id == item.id) it.copy(isFavorite = !it.isFavorite)
                            else it
                        }
                    }
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