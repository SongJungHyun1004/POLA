package com.jinjinjara.pola.presentation.ui.screen.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R
import kotlinx.coroutines.launch

data class TimelineItem(
    val date: String,
    val dayOfWeek: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(modifier: Modifier = Modifier) {
    val categories = listOf("전체", "여행", "음식", "일상", "친구", "가족")
    var selectedCategory by remember { mutableStateOf("전체") }

    val timelineItems = listOf(
        TimelineItem("25.10.20", "MON"),
        TimelineItem("25.10.21", "TUE"),
        TimelineItem("25.10.22", "WED"),
        TimelineItem("25.10.23", "THU"),
        TimelineItem("25.10.24", "FRI")
    )

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTopButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Timeline",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO */ }) {
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
                        onCategorySelected = { category -> selectedCategory = category }
                    )
                }

                // 각 타임라인 아이템
                itemsIndexed(timelineItems) { index, item ->
                    Box(modifier = Modifier.padding(start = 16.dp, top = if (index == 0) 8.dp else 0.dp)) {
                        TimelineItem(
                            date = item.date,
                            dayOfWeek = item.dayOfWeek,
                            isFirst = index == 0,
                            isLast = index == timelineItems.lastIndex
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }

        // 맨 위로 버튼
        if (showScrollToTopButton) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch { listState.animateScrollToItem(0) }
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
    Row(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.width(8.dp))
        categories.forEach { category ->
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
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.tertiary
                )
            }
        }
        Spacer(Modifier.width(16.dp))
    }
}

@Composable
fun TimelineItem(
    date: String,
    dayOfWeek: String,
    isFirst: Boolean,
    isLast: Boolean
) {
    val filmHeight = 150.dp

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 왼쪽 세로선
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(5.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else {
                Spacer(modifier = Modifier.height(5.dp))
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(50))
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        // 날짜 & 필름
        Column {
            Row(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = date,
                    fontSize = 18.sp,
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

            // 필름 스크롤 + 그라데이션
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .height(filmHeight)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.width(12.dp))
                    repeat(10) {
                        val painter = painterResource(R.drawable.film)
                        val ratio = painter.intrinsicSize.width / painter.intrinsicSize.height

                        Box(
                            modifier = Modifier
                                .height(filmHeight)
                                .aspectRatio(ratio)
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = "Recents Film",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
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

                // 왼쪽 그라데이션 (겹치기)
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}
