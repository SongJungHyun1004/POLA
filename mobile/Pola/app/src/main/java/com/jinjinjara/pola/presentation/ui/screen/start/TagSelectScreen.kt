package com.jinjinjara.pola.presentation.ui.screen.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TagSelectScreen(
    onNextClick: (Set<String>) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var currentCategory by remember { mutableStateOf<TagCategory?>(null) }
    var customTagsMap by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }

    val categories = remember(customTagsMap) {
        listOf(
            TagCategory(
                title = "간식",
                tags = listOf("카페", "디저트", "스낵", "베이커리", "음료") + (customTagsMap["간식"] ?: emptyList())  // 추가
            ),
            TagCategory(
                title = "장소",
                tags = listOf("포토존", "공원", "루프탑", "전망대", "힐링스팟") + (customTagsMap["장소"] ?: emptyList())  // 추가
            ),
            TagCategory(
                title = "정보",
                tags = listOf("핫플", "체험", "전시", "이벤트", "문화시설") + (customTagsMap["정보"] ?: emptyList())  // 추가
            ),
            TagCategory(
                title = "쇼핑",
                tags = listOf("컨셉스토어", "플리마켓", "빈티지샵", "쇼핑몰", "서점") + (customTagsMap["쇼핑"] ?: emptyList())  // 추가
            ),
            TagCategory(
                title = "커스텀",
                tags = customTagsMap["커스텀"] ?: emptyList()  // 수정
            )
        )
    }

    // 모든 태그를 초기 선택 상태로 설정
    val allTags = remember(categories) { categories.flatMap { it.tags }.toSet() }
    var selectedTags by remember { mutableStateOf(allTags) }

    LaunchedEffect(categories) {
        selectedTags = selectedTags + categories.flatMap { it.tags }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Title
            Text(
                text = buildAnnotatedString {
                    append("카테고리에 담고 싶은\n")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                        )
                    ) {
                        append("태그")
                    }
                    append("를 입력해주세요")
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
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
                        onClearAll = {
                            selectedTags = selectedTags - category.tags.toSet()
                        },
                        onAddClick = {
                            currentCategory = category
                            showAddDialog = true
                        },
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Next Button
            Button(
                onClick = { onNextClick(selectedTags) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = "다음",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showAddDialog && currentCategory != null) {
        AddTagDialog(
            categoryTitle = currentCategory?.title ?: "커스텀",
            onDismiss = { showAddDialog = false },
            onConfirm = { newTags ->
                customTagsMap = customTagsMap + (currentCategory!!.title to
                        ((customTagsMap[currentCategory!!.title] ?: emptyList()) + newTags))
                selectedTags = selectedTags + newTags.toSet()
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TagCategorySection(
    category: TagCategory,
    selectedTags: Set<String>,
    onTagClick: (String) -> Unit,
    onClearAll: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 해당 카테고리에서 선택된 태그 개수 계산
    val selectedCount = category.tags.count { selectedTags.contains(it) }

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
                text = "모두 해제",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClearAll() }
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

            AddButton(onClick = onAddClick)
        }


        // 경고 메시지
        val minimumTags = 5
        if (selectedCount < minimumTags) {
            Text(
                text = "${minimumTags}개 이상 선택해주세요.",
                fontSize = 12.sp,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$text",
            fontSize = 14.sp,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun AddButton(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp), // horizontal padding 증가
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
fun AddTagDialog(
    categoryTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var addedTags by remember { mutableStateOf(listOf<String>()) }

    AlertDialog(
        modifier = Modifier.width(250.dp),
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$categoryTitle 태그 추가",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { newValue ->
                        if (newValue.endsWith(" ") && text.isNotBlank()) {
                            // 스페이스바가 입력되면 태그 추가
                            val newTag = text.trim()
                            if (newTag.isNotEmpty()) {
                                addedTags = addedTags + newTag
                                text = ""
                            }
                        } else {
                            text = newValue
                        }
                    },
                    placeholder = {
                        Text(
                            text = "태그 입력 후 스페이스바 입력",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )

                // 추가된 태그들 표시
                if (addedTags.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        addedTags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        "취소",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                TextButton(
                    onClick = {
                        if (addedTags.isNotEmpty()) {
                            onConfirm(addedTags)
                        }
                    },
                    enabled = addedTags.isNotEmpty()
                ) {
                    Text(
                        "확인",
                        color = if (addedTags.isNotEmpty()) MaterialTheme.colorScheme.tertiary else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(16.dp)
    )
}

data class TagCategory(
    val title: String,
    val tags: List<String>,
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