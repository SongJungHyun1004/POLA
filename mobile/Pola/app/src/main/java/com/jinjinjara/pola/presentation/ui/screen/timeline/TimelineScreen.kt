package com.jinjinjara.pola.presentation.ui.screen.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R

// 타임라인 아이템 데이터 클래스
data class TimelineItem(
    val date: String,
    val dayOfWeek: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier
) {
    // 카테고리 목록과 선택된 카테고리 상태
    val categories = listOf("전체", "여행", "음식", "일상", "친구", "가족")
    var selectedCategory by remember { mutableStateOf("전체") }

    // 임시 타임라인 데이터
    val timelineItems = listOf(
        TimelineItem("25.10.20", "MON"),
        TimelineItem("25.10.21", "TUE"),
        TimelineItem("25.10.22", "WED"),
        TimelineItem("25.10.20", "MON"),
        TimelineItem("25.10.21", "TUE"),
        TimelineItem("25.10.22", "WED")

    )

    // 스크롤 상태 추적
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 스크롤 위치가 첫 번째 아이템을 벗어났는지 확인
    val showScrollToTopButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                TopAppBar(
                    modifier = Modifier.height(48.dp),
                    title = {
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Timeline",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: 클릭 이벤트 처리 */ }) {
                            Image(
                                painter = painterResource(id = R.drawable.calendar),
                                contentDescription = "달력 아이콘",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    windowInsets = WindowInsets(0.dp)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    CategoryChips(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            selectedCategory = category
                        }
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                itemsIndexed(timelineItems) { index, item ->
                    Box(modifier = Modifier.padding(start = 16.dp)) {
                        TimelineItem(
                            date = item.date,
                            dayOfWeek = item.dayOfWeek,
                            isFirst = index == 0,
                            isLast = index == timelineItems.lastIndex
                        )
                    }
                }

                item { Spacer(Modifier.height(48.dp)) }
            }
        }

        // 맨 위로 스크롤 버튼
        if (showScrollToTopButton) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
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

@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                shadowElevation = if (isSelected) 4.dp else 2.dp,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onCategorySelected(category) }
            ) {
                Text(
                    text = category,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun TimelineItem(
    date: String,
    dayOfWeek: String,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
            // 타임라인 세로선
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // 세로선 (첫번째 아이템이 아닐 때만 표시)
                if (!isFirst) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                // 원 부분
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 작은 원 (background 색상)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
                // 세로선 (마지막 아이템이 아닐 때만 표시)
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(140.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // 날짜 & 필름 영역
            Column(
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // 날짜
                Row(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = date,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = " $dayOfWeek",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(120.dp)
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        // 스크롤되는 아이템들
                        Spacer(Modifier.width(12.dp))
                        repeat(10) {    // 나중에 리스트 갯수만큼 받아와야함.
                            Box(
                                modifier = Modifier
                                    .height(120.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.film),
                                    contentDescription = "Recents Film",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .align(Alignment.Center)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.temp_image),
                                        contentDescription = "Content",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                    }

                    // 왼쪽 그라데이션 오버레이
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(120.dp)
                            .align(Alignment.CenterStart)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }
    }

