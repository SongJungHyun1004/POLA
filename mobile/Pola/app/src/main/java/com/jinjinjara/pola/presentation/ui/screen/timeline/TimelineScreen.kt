package com.jinjinjara.pola.presentation.ui.screen.timeline

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jinjinjara.pola.R
import com.jinjinjara.pola.domain.model.TimelineFile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val showScrollToTopButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    // 에러 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 무한 스크롤 감지
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val totalItemCount = listState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && lastVisibleIndex >= totalItemCount - 2) {
                    if (uiState is TimelineUiState.Success && (uiState as TimelineUiState.Success).canLoadMore) {
                        viewModel.loadMore()
                    }
                }
            }
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
            when (uiState) {
                is TimelineUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is TimelineUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (uiState as TimelineUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("다시 시도")
                            }
                        }
                    }
                }

                is TimelineUiState.Success -> {
                    val successState = uiState as TimelineUiState.Success
                    val groupedFiles = successState.groupedFiles

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        item {
                            TimelineCategoryChips(
                                categories = categories,
                                selectedCategoryId = selectedCategoryId,
                                onCategorySelected = { categoryId ->
                                    viewModel.selectCategory(categoryId)
                                }
                            )
                        }

                        if (groupedFiles.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 64.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.empty),
                                            contentDescription = "No content",
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
                        } else {
                            val dateKeys = groupedFiles.keys.toList()
                            itemsIndexed(dateKeys) { index, dateKey ->
                                val files = groupedFiles[dateKey] ?: emptyList()
                                Box(modifier = Modifier.padding(start = 16.dp, top = if (index == 0) 8.dp else 0.dp)) {
                                    TimelineItem(
                                        dateLabel = dateKey,
                                        files = files,
                                        isFirst = index == 0,
                                        isLast = index == dateKeys.lastIndex && !successState.canLoadMore
                                    )
                                }
                            }

                            // 로딩 중 인디케이터
                            if (successState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }

                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
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
fun TimelineCategoryChips(
    categories: List<com.jinjinjara.pola.domain.model.Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.width(8.dp))

        // "전체" 버튼
        val isAllSelected = selectedCategoryId == null
        Surface(
            shape = RoundedCornerShape(50),
            color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
            shadowElevation = if (isAllSelected) 4.dp else 2.dp,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onCategorySelected(null) }
        ) {
            Text(
                text = "전체",
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                color = if (isAllSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.tertiary
            )
        }

        // 카테고리 버튼들
        categories.forEach { category ->
            val isSelected = category.id == selectedCategoryId
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                shadowElevation = if (isSelected) 4.dp else 2.dp,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onCategorySelected(category.id) }
            ) {
                Text(
                    text = category.name,
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
    dateLabel: String,
    files: List<TimelineFile>,
    isFirst: Boolean,
    isLast: Boolean
) {
    val filmHeight = 150.dp

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
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
            // 날짜 파싱 (예: "25.11.02 SAT")
            val dateParts = dateLabel.split(" ")
            val date = dateParts.getOrNull(0) ?: ""
            val dayOfWeek = dateParts.getOrNull(1) ?: ""

            Row(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = date,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                if (dayOfWeek.isNotEmpty()) {
                    Text(
                        text = " $dayOfWeek",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
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
                    files.forEach { file ->
                        val painter = painterResource(R.drawable.film)
                        val ratio = painter.intrinsicSize.width / painter.intrinsicSize.height

                        Box(
                            modifier = Modifier
                                .height(filmHeight)
                                .aspectRatio(ratio)
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = "Film Frame",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .align(Alignment.Center)
                            ) {
                                AsyncImage(
                                    model = file.imageUrl,
                                    contentDescription = "Content Image",
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
