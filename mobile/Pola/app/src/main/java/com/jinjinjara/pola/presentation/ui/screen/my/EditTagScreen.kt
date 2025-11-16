package com.jinjinjara.pola.presentation.ui.screen.my

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.jinjinjara.pola.domain.model.Tag
import com.jinjinjara.pola.util.ErrorType

@Composable
fun EditTagScreen(
    categoriesWithTags: Map<String, List<Tag>> = emptyMap(),
    onEditComplete: (Map<String, List<String>>, Set<String>) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    viewModel: EditTagViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 모든 태그 ID를 초기 선택
    LaunchedEffect(categoriesWithTags) {
        if (categoriesWithTags.isNotEmpty()) {
            val allTagIds = categoriesWithTags.values.flatten().map { it.id }
            viewModel.initializeSelectedTagIds(allTagIds)
        }
    }

    // 뒤로가기 처리
    BackHandler {
        onBackClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = buildAnnotatedString {
                    append("원하는 ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                        )
                    ) {
                        append("태그")
                    }
                    append("를\n선택하거나 해제하세요.")
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Handle different UI states
            when (val state = uiState) {
                is EditTagUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
            }

                is EditTagUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = {
                                // 에러 상태 초기화 후 다시 시도할 수 있도록
                                viewModel.resetState()
                            }) {
                                Text("다시 시도")
                            }
                        }
                    }
                }

                else -> {
                    TagSelectContent(
                        categoriesWithTags = categoriesWithTags,
                        onSubmit = { categories, selectedTags ->
                            onEditComplete(categories, selectedTags)
                        },
                        onBackClick = onBackClick,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun TagSelectContent(
    categoriesWithTags: Map<String, List<Tag>>,
    onSubmit: (Map<String, List<String>>, Set<String>) -> Unit,
    onBackClick: () -> Unit = {},
    viewModel: EditTagViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var currentCategory by remember { mutableStateOf<TagCategory?>(null) }

    // ViewModel에서 상태 가져오기
    val selectedTagIdsList by viewModel.selectedTagIds.collectAsState()
    val customTagsMap by viewModel.customTagsMap.collectAsState()

    // List를 Set으로 변환
    val selectedTagIds = remember(selectedTagIdsList) { selectedTagIdsList.toSet() }

    // 선택된 카테고리를 TagCategory로 변환
    val categories = remember(categoriesWithTags, customTagsMap) {
        categoriesWithTags.map { (categoryName, tags) ->
            // 커스텀 태그 추가 (임시 ID 사용)
            var nextTempId = -1L
            val customTags = (customTagsMap[categoryName] ?: emptyList()).map { tagName ->
                Tag(id = nextTempId--, name = tagName)
            }

            TagCategory(
                title = categoryName,
                tags = tags + customTags
            )
        }
    }

    Column {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            categories.forEach { category ->
                TagCategorySection(
                    category = category,
                    selectedTagIds = selectedTagIds,
                    onTagClick = { tag ->
                        viewModel.toggleTag(tag.id)
                    },
                    onClearAll = {
                        val count = category.tags.count { selectedTagIds.contains(it.id) }
                        if (count == 0) {
                            viewModel.selectAllTagsInCategory(category.tags.map { it.id })  // 모두 선택
                        } else {
                            viewModel.deselectAllTagsInCategory(category.tags.map { it.id })  // 모두 해제
                        }
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

        // 모든 카테고리가 최소 4개 이상 선택되었는지 검증
        val allCategoriesMeetRequirement = remember(selectedTagIds, categories) {
            categories.all { category ->
                category.tags.count { selectedTagIds.contains(it.id) } >= 4
            }
        }

        // Buttons Row (이전, 완료)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 이전 버튼
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = "이전",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 완료 버튼
            Button(
                onClick = {
                    // 완료 버튼 기능은 비워둠
                    //onEditComplete(categoriesWithTags, selectedTags)
                },
                enabled = allCategoriesMeetRequirement,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = "완료",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showAddDialog && currentCategory != null) {
        AddTagDialog(
            categoryTitle = currentCategory?.title ?: "커스텀",
            existingTags = currentCategory?.tags ?: emptyList(),
            onDismiss = { showAddDialog = false },
            onConfirm = { newTags ->
                // ViewModel을 통해 커스텀 태그 추가 (자동으로 선택됨)
                viewModel.addCustomTags(currentCategory!!.title, newTags)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TagCategorySection(
    category: TagCategory,
    selectedTagIds: Set<Long>,
    onTagClick: (Tag) -> Unit,
    onClearAll: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 해당 카테고리에서 선택된 태그 개수 계산
    val selectedCount = category.tags.count { selectedTagIds.contains(it.id) }
    val noneSelected = selectedCount == 0
    val buttonText = if (noneSelected) "모두 선택" else "모두 해제"

    Column(modifier = modifier) {
        // Category Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = buttonText,
                fontSize = 16.sp,
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
                    text = tag.name,
                    isSelected = selectedTagIds.contains(tag.id),
                    onClick = { onTagClick(tag) }
                )
            }

            AddButton(onClick = onAddClick)
        }


        // 경고 메시지 (모든 카테고리에 표시)
        val minimumTags = 4
        if (selectedCount < minimumTags) {
            Text(
                text = "4개 이상 선택해주세요.",
                fontSize = 14.sp,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        else {
            Text(
                text = " ",
                fontSize = 14.sp,
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
    existingTags: List<Tag> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var addedTags by remember { mutableStateOf(listOf<String>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val existingTagNames = remember(existingTags) { existingTags.map { it.name } }

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
                        // 스페이스바 또는 엔터 키 감지
                        if ((newValue.endsWith(" ") || newValue.endsWith("\n")) && text.isNotBlank()) {
                            val newTag = text.trim()
                            when {
                                newTag.isEmpty() -> {
                                    text = ""
                                }
                                existingTagNames.contains(newTag) -> {
                                    errorMessage = "이미 존재하는 태그입니다"
                                    text = ""
                                }
                                addedTags.contains(newTag) -> {
                                    errorMessage = "이미 추가한 태그입니다"
                                    text = ""
                                }
                                else -> {
                                    addedTags = addedTags + newTag
                                    text = ""
                                    errorMessage = null
                                }
                            }
                        } else {
                            text = newValue
                            if (errorMessage != null) {
                                errorMessage = null
                            }
                        }
                    },
                    placeholder = {
                        Text(
                            text = "태그 입력 후 스페이스바",
                            fontSize = 12.sp,
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
                    ),
                    isError = errorMessage != null,
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

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
    val tags: List<Tag>,
)
