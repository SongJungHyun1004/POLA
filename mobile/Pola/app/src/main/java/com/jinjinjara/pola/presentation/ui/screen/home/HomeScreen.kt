package com.jinjinjara.pola.presentation.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import com.jinjinjara.pola.presentation.ui.component.PolaSearchBar

data class RecentItem(
    val type: String,
    val title: String,
    val subtitle: String,
    val imageRes: Int? = null
)

data class Category(
    val name: String,
    val imageRes: Int
)

@Composable
fun HomeScreen() {
    val recentItems = remember {
        listOf(
            RecentItem("BLOG", "Lorem ipsum", "dolor sit amet, consectetur adipiscing elit..."),
            RecentItem("BLOG", "Lorem ipsum", "dolor sit amet, consectetur adipiscing elit..."),
            RecentItem("NOTE", "Lorem ipsum", "dolor sit amet...")
        )
    }

    val categories = remember {
        listOf(
            Category("쇼핑", 0),
            Category("장소", 0),
            Category("인물", 0),
            Category("간식", 0),
            Category("쇼핑", 0),
            Category("장소", 0),
            Category("인물", 0),
            Category("간식", 0),
        )
    }

    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
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
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.star),
                    contentDescription = "Favorites",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .clickable {
                            // 즐겨찾기 이동
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
                    // 추후 recent 리스트로 변경
                    repeat(10) {
                        val painter = painterResource(R.drawable.film)
                        val ratio = painter.intrinsicSize.width / painter.intrinsicSize.height
                        Box(
                            modifier = Modifier
                                .height(150.dp)
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = {}) {
                        Text(
                            text = "전체보기",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Categories Grid - LazyColumn items로 변경
            items(categories.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (item in rowItems) {
                        CategoryCard(item, Modifier.weight(1f))
                    }

                    // 홀수 개일 경우 오른쪽 칸 비워주기
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 카드 스택 영역
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // 뒤에서부터 3장의 카드를 겹쳐서 표시
            // 오른쪽 뒤 카드
            PolaCard(
                modifier = Modifier
                    .height(100.dp)
                    .graphicsLayer {
                        rotationZ = 20f
                        translationX = 55f
                        translationY = -15f
                        shadowElevation = 8.dp.toPx()
                    },
                ratio = 0.7816f,
                paddingValues = PaddingValues(
                    top = 8.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
                imageResId = R.drawable.temp_image
            )

            // 왼쪽 뒤 카드
            PolaCard(
                modifier = Modifier
                    .height(100.dp)

                    .graphicsLayer {
                        rotationZ = -25f
                        translationX = -45f
                        translationY = -15f
                        shadowElevation = 8.dp.toPx()
                    },
                ratio = 0.7816f,
                paddingValues = PaddingValues(
                    top = 8.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
                imageResId = R.drawable.temp_image
            )

            // 중간 카드
            PolaCard(
                modifier = Modifier
                    .height(100.dp)
                    .shadow(elevation = 8.dp),
                ratio = 0.7816f,
                paddingValues = PaddingValues(
                    top = 8.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
                imageResId = R.drawable.temp_image
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 카테고리 이름
        Text(
            text = category.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
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