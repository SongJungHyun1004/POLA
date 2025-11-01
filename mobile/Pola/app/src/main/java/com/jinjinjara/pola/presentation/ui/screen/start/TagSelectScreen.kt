package com.jinjinjara.pola.presentation.ui.screen.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectScreen(
    onNextClick: (Set<String>) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val categories = listOf(
        TagCategory(
            title = "식음료",
            tags = listOf("카페", "음식점", "디저트 / 베이커리", "바")
        ),
        TagCategory(
            title = "여행",
            tags = listOf("자연", "포토존", "관광지", "관광지", "지")
        ),
        TagCategory(
            title = "종교시설",
            tags = listOf("광장"),
            hasAddButton = true
        ),
        TagCategory(
            title = "쇼핑",
            tags = listOf("숍", "서점", "소핑몰", "시장"),
            hasAddButton = true
        ),
        TagCategory(
            title = "커스텀",
            tags = listOf("#관광지", "관광지"),
            hasAddButton = true
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            // Title
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("카테고리에 담고 싶은\n")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFFB8956A),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("태그를 입력해주세요")
                    }
                },
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                categories.forEach { category ->
                    TagCategorySection(
                        category = category,
                        selectedTags = selectedTags,
                        onTagClick = { tag ->
                            selectedTags = if (selectedTags.contains(tag)) {
                                selectedTags - tag
                            } else {
                                selectedTags + tag
                            }
                        },
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }

            // Next Button
            Button(
                onClick = { onNextClick(selectedTags) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB8956A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "다음",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TagCategorySection(
    category: TagCategory,
    selectedTags: Set<String>,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Category Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = "모두 선택",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Tags
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            category.tags.forEach { tag ->
                TagChip(
                    text = tag,
                    isSelected = selectedTags.contains(tag),
                    onClick = { onTagClick(tag) }
                )
            }

            if (category.hasAddButton) {
                AddButton()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = 14.sp
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            selectedContainerColor = Color(0xFFB8956A),
            labelColor = Color.Black,
            selectedLabelColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddButton() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = Color(0xFFB8956A),
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

data class TagCategory(
    val title: String,
    val tags: List<String>,
    val hasAddButton: Boolean = false
)

@Preview(showBackground = true)
@Composable
fun TagSelectScreenPreview() {
    MaterialTheme {
        TagSelectScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun TagChipPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TagChip(text = "카페", isSelected = false, onClick = {})
            TagChip(text = "음식점", isSelected = true, onClick = {})
        }
    }
}