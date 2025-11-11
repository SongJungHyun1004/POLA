package com.jinjinjara.pola.presentation.ui.screen.start

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import com.jinjinjara.pola.domain.model.CategoryRecommendation

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val isAddBtn: Boolean = false
)

// Icon mapping function
private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "쇼핑", "shopping" -> Icons.Outlined.ShoppingBag
        "장소", "place", "위치" -> Icons.Outlined.Place
        "인물", "person", "사람" -> Icons.Outlined.Person
        "간식", "snack", "디저트" -> Icons.Outlined.BakeryDining
        "운동", "exercise", "피트니스" -> Icons.Outlined.FitnessCenter
        "정보", "info" -> Icons.Outlined.Info
        "학습", "study", "교육" -> Icons.Outlined.School
        "음식", "food", "한식", "야식" -> Icons.Outlined.Restaurant
        "여행", "travel" -> Icons.Default.Flight
        "가족", "family" -> Icons.Default.FamilyRestroom
        "사진", "photo" -> Icons.Default.PhotoCamera
        "취미", "hobby" -> Icons.Default.SportsEsports
        else -> Icons.Outlined.Category // Default icon
    }
}

@Composable
fun CategorySelectScreen(
    onCategorySelected: (Map<String, List<String>>) -> Unit,
    viewModel: CategorySelectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val showExitToast by viewModel.showExitToast.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    // 뒤로가기 처리
    BackHandler {
        val shouldExit = viewModel.onBackPressed()
        if (shouldExit) {
            // 두 번째 뒤로가기 -> 앱 종료
            activity?.finish()
        }
    }

    // 토스트 표시
    LaunchedEffect(showExitToast) {
        if (showExitToast) {
            Toast.makeText(
                context,
                "뒤로가기를 한 번 더 누르면 앱이 종료됩니다",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetExitToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title
        Text(
            text = buildAnnotatedString {
                append("원하는 ")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("카테고리")
                }
                append("를\n모두 골라주세요")
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Handle different UI states
        when (val state = uiState) {
            is CategoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is CategoryUiState.Error -> {
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
                        Button(onClick = { viewModel.retry() }) {
                            Text("다시 시도")
                        }
                    }
                }
            }

            is CategoryUiState.Success -> {
                CategoryContent(
                    categories = state.categories,
                    selectedCategories = selectedCategories,
                    onCategoryToggle = { viewModel.toggleCategory(it) },
                    onCategorySelected = {
                        // 선택된 카테고리와 해당 태그 정보를 전달
                        val categoriesWithTags = viewModel.getSelectedCategoriesWithTags()
                        onCategorySelected(categoriesWithTags)
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryContent(
    categories: List<CategoryRecommendation>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onCategorySelected: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    // rememberSaveable을 사용하여 커스텀 카테고리 이름 저장 (재구성 시에도 유지)
    var customCategoryNames by rememberSaveable { mutableStateOf(listOf<String>()) }

    // 커스텀 카테고리 이름을 CategoryRecommendation으로 변환
    val customCategories = remember(customCategoryNames) {
        customCategoryNames.map { CategoryRecommendation(categoryName = it, tags = emptyList()) }
    }

    // Map API categories to UI categories with icons
    val uiCategories = remember(categories, customCategories) {
        (categories + customCategories).map { category ->
            Category(
                id = category.categoryName,
                name = category.categoryName,
                icon = getCategoryIcon(category.categoryName),
                isAddBtn = false
            )
        } + Category(
            id = "add",
            name = "",
            icon = Icons.Default.Add,
            isAddBtn = true
        )
    }

    Column {
        // 카테고리 선택 검증 메시지
        if (selectedCategories.size < 2) {
            Text(
                text = "2개 이상 선택해주세요",
                fontSize = 14.sp,
                color = Color.Red
            )
        } else {
            Text(
                text = " ",
                fontSize = 14.sp,
                color = Color.Red
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        val rowCount = (uiCategories.size + 2) / 3
        // Category Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            for (rowIndex in 0 until rowCount) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (colIndex in 0..2) {
                        val index = rowIndex * 3 + colIndex
                        if (index < uiCategories.size) {
                            val category = uiCategories[index]
                            CategoryItem(
                                category = category,
                                isSelected = selectedCategories.contains(category.id),
                                onToggle = { onCategoryToggle(category.id) },
                                onAddClick = { showAddDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Next Button
        Button(
            onClick = onCategorySelected,
            enabled = selectedCategories.size >= 2,
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

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { categoryName ->
                customCategoryNames = customCategoryNames + categoryName
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        Color(0xFFFFF6EA)
    } else {
        Color.White
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else if (category.isAddBtn) {
        MaterialTheme.colorScheme.tertiary
    } else {
        Color(0xFFE3E3E3)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable {
                if (category.isAddBtn) {
                    onAddClick()
                } else {
                    onToggle()
                }
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!category.isAddBtn) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(alignment = Alignment.CenterStart)
                    .padding(start = 8.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = category.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        // 카테고리 추가 버튼
        else {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }

        // Checkmark for selected items
        if (!category.isAddBtn) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color(
                        0xFFE3E3E3
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val maxLength = 8

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
                    text = "카테고리 추가",
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
                    onValueChange = {
                        if (it.length <= maxLength) text = it
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
                    trailingIcon = {
                        Text(
                            text = "${text.length}/$maxLength",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
                        if (text.isNotBlank()) {
                            onConfirm(text)
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Text(
                        "확인",
                        color = if (text.isNotBlank()) MaterialTheme.colorScheme.tertiary else Color.Gray,
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

@Preview(showBackground = true)
@Composable
fun CategorySelectScreenPreview() {
    CategorySelectScreen(
        onCategorySelected = { _ -> }
    )
}

@Preview(showBackground = true)
@Composable
fun AddCategoryDialogPreview() {
    MaterialTheme {
        AddCategoryDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}