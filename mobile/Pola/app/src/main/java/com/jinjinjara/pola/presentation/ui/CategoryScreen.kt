package com.jinjinjara.pola.presentation.ui

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

data class CategoryItem(
    val id: String,
    val name: String
)

@Composable
fun CategoryScreen() {
    var selectedTab by remember { mutableStateOf("간식") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("최신순") }

    val categories = listOf(
        CategoryItem("1", "반찬"),
        CategoryItem("2", "초코"),
        CategoryItem("3", "딸기")
    )

    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
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
                modifier = Modifier.size(30.dp)
            )

            Text(
                text = "간식",
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

        // Tab Row
        CategoryChips(
            categories = listOf("전체", "창소", "인물", "간식"),
            selectedCategory = selectedTab,
            onCategorySelected = { selectedTab = it }
        )

        // Grid Icon and Sort Menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = "그리드",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
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
                    listOf("태그순", "최신순", "오래된순").forEach { sort ->
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

        // Category Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                PolaCard(
                    modifier = Modifier
                        .shadow(elevation = 8.dp),
                    ratio = 0.7661f,
                    imageRatio = 0.9062f,
                    paddingValues = PaddingValues(
                        top = 4.dp,
                        start = 4.dp,
                        end = 4.dp
                    ),
                    imageResId = R.drawable.temp_image,
                    textList = listOf(category.name),
                    textSize = 12.sp,
                    textSpacing = 8.dp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    CategoryScreen()
}