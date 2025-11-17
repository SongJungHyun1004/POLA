package com.jinjinjara.pola.presentation.ui.screen.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jinjinjara.pola.R
import com.jinjinjara.pola.domain.model.CategoryInfo
import com.jinjinjara.pola.domain.model.HomeScreenData
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import com.jinjinjara.pola.presentation.ui.component.PolaSearchBar
import com.jinjinjara.pola.presentation.ui.component.SearchBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

data class Category(
    val name: String,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToContents: (Long) -> Unit = {},
    onNavigateToCategory: (Long) -> Unit = {},
    onNavigateToFavorite: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToChatbot: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    LaunchedEffect(uiState) {
        if (uiState is HomeUiState.Success) {
            isRefreshing = false
        }
    }

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is HomeUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.message)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadHomeData() }) {
                        Text("다시 시도")
                    }
                }
            }
        }

        is HomeUiState.Success -> {
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
                        modifier = Modifier.align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.background,
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                HomeContent(
                    homeData = state.data,
                    onNavigateToContents = onNavigateToContents,
                    onNavigateToCategory = onNavigateToCategory,
                    onNavigateToFavorite = onNavigateToFavorite,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToChatbot = onNavigateToChatbot
                )
            }
        }
    }

}

@Composable
private fun HomeContent(
    homeData: HomeScreenData,
    onNavigateToContents: (Long) -> Unit = {},
    onNavigateToCategory: (Long) -> Unit,
    onNavigateToFavorite: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToChatbot: () -> Unit
) {

    val isEmpty =
        homeData.timeline.isEmpty() && homeData.categories.all { it.recentFiles.isEmpty() }

    if (isEmpty) {
        // Empty 상태 표시
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 챗봇 버튼
                    Image(
                        painter = painterResource(R.drawable.pola_chatbot),
                        contentDescription = "챗봇",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onNavigateToChatbot()
                            },
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    SearchBar(
                        searchText = "",
                        onSearchClick = { onNavigateToSearch() },
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
            }

            // Empty 이미지
            Box(
                modifier = Modifier.fillMaxSize(),
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
                        text = "정리할 컨텐츠가 없어요",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 챗봇 버튼
                Image(
                    painter = painterResource(R.drawable.pola_chatbot),
                    contentDescription = "챗봇",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onNavigateToChatbot()
                        },
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(12.dp))

                SearchBar(
                    searchText = "",
                    onSearchClick = { onNavigateToSearch() },
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
        }

        // Content - LazyColumn으로 전체 변경
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Recents Section
            item {
                Text(
                    text = "Recents",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Film Strip Style Recent Items
            item {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.width(16.dp))
                    repeat(10) { index ->
                        val fileInfo = homeData.timeline.getOrNull(index)
                        val painter = painterResource(R.drawable.film)
                        val ratio = painter.intrinsicSize.width / painter.intrinsicSize.height
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .aspectRatio(ratio)
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = "Recents Film",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            // 실제 데이터가 있을 때만 이미지 표시
                            fileInfo?.let {

                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .align(Alignment.Center)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onNavigateToContents(fileInfo.id)
                                        }
                                ) {
                                    when {
                                        it.type.startsWith("image") == true -> {
                                            AsyncImage(
                                                model = it.imageUrl,
                                                contentDescription = "Content",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        it.type.startsWith("text") == true -> {
                                            var textContent by remember {
                                                mutableStateOf<String?>(
                                                    null
                                                )
                                            }

                                            LaunchedEffect(it.imageUrl) {
                                                try {
                                                    textContent = withContext(Dispatchers.IO) {
                                                        URL(it.imageUrl).readText(Charsets.UTF_8)
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
                                                    .padding(6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = textContent ?: "로딩 중...",
                                                    color = Color.DarkGray,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 4,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        else -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFFE0E0E0)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.empty),
                                                    contentDescription = "Unknown file",
                                                    tint = Color.DarkGray,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Categories Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onNavigateToCategory(-1L) // 전체 보기 임시 id
                            },
                        text = "전체보기",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(36.dp))
            }

            // Categories Grid - LazyColumn items로 변경
            items(homeData.categories.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (item in rowItems) {
                        CategoryCard(
                            category = item,
                            Modifier.weight(1f),
                            onClick = { onNavigateToCategory(item.id) }
                        )
                    }

                    // 홀수 개일 경우 오른쪽 칸 비워주기
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

        }
    }

}

@Composable
fun CategoryCard(
    category: CategoryInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val files = category.recentFiles

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 카드 스택 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (files.isEmpty()) {
                            Toast.makeText(context, "수집된 컨텐츠가 없습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            onClick()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (files.isEmpty()) {
                // 파일이 없을 경우 흐린 폴라 한 장 표시
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(5.dp),
                            clip = false
                        )
                ) {
                    PolaCard(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.4f),
                        ratio = 0.7816f,
                        imageRatio = 0.9152f,
                        paddingValues = PaddingValues(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                        imageResId = R.drawable.empty,
                        type = null
                    )
                }
            } else {
                // 뒤에서부터 3장의 카드를 겹쳐서 표시
                // 오른쪽 뒤 카드
                files.getOrNull(2)?.let { fileInfo ->
                    PolaCard(
                        modifier = Modifier
                            .height(120.dp)
                            .graphicsLayer {
                                rotationZ = 20f
                                translationX = 55f
                                translationY = -15f
                                shadowElevation = 8.dp.toPx()
                            },
                        ratio = 0.7816f,
                        imageRatio = 0.9152f,
                        paddingValues = PaddingValues(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                        imageUrl = fileInfo.imageUrl,
                        type = fileInfo.type,
                    )
                }

                // 왼쪽 뒤 카드
                files.getOrNull(1)?.let { fileInfo ->
                    PolaCard(
                        modifier = Modifier
                            .height(120.dp)
                            .graphicsLayer {
                                rotationZ = -25f
                                translationX = -45f
                                translationY = -15f
                                shadowElevation = 8.dp.toPx()
                            },
                        ratio = 0.7816f,
                        imageRatio = 0.9152f,
                        paddingValues = PaddingValues(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                        imageUrl = fileInfo.imageUrl,
                        type = fileInfo.type,
                    )
                }

                // 중간 카드
                files.getOrNull(0)?.let { fileInfo ->
                    PolaCard(
                        modifier = Modifier
                            .height(120.dp),
                        ratio = 0.7816f,
                        imageRatio = 0.9152f,
                        paddingValues = PaddingValues(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                        imageUrl = fileInfo.imageUrl,
                        type = fileInfo.type,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 카테고리 이름
        Text(
            modifier = if (files.isEmpty()) Modifier.alpha(0.4f) else Modifier,
            text = category.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}