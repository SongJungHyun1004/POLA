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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
            Category("소펀", 0),
            Category("창소", 0),
            Category("인물", 0),
            Category("간식", 0)
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

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Recents Section
            Text(
                text = "Recents",
                fontSize = 24.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Film Strip Style Recent Items

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
            ) {
                // 스크롤되는 아이템들
                Spacer(Modifier.width(16.dp))
                repeat(10) {
                    Box(
                        modifier = Modifier
                            .height(120.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.film),
                            contentDescription = "Film decoration",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit // 또는 ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
            }


//            Surface(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                shape = RoundedCornerShape(8.dp),
//                color = Color.Black
//            ) {
//                Column {
//                    // Film perforations top
//                    FilmPerforations()
//
//                    LazyRow(
//                        modifier = Modifier.padding(vertical = 8.dp),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        contentPadding = PaddingValues(horizontal = 8.dp)
//                    ) {
//                        items(recentItems) { item ->
//                            RecentItemCard(item)
//                        }
//                    }
//
//                    // Film perforations bottom
//                    FilmPerforations()
//                }
//            }

            Spacer(modifier = Modifier.height(24.dp))

            // Categories Section
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

            Spacer(modifier = Modifier.height(16.dp))

            // Categories Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryCard(categories[0], Modifier.weight(1f))
                    CategoryCard(categories[1], Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryCard(categories[2], Modifier.weight(1f))
                    CategoryCard(categories[3], Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FilmPerforations() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(20) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(12.dp)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun RecentItemCard(item: RecentItem) {
    Surface(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp),
        shape = RoundedCornerShape(4.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Placeholder for image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.type == "NOTE") {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.type,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = item.subtitle,
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 2
            )
        }
    }
}

@Composable
fun CategoryCard(category: Category, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Card stack effect
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                ) {
                    // Back cards
                    repeat(3) { index ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(
                                    x = (index * 4).dp,
                                    y = (index * 4).dp
                                ),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            shadowElevation = (3 - index).dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF0F0F0))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BlogHomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}