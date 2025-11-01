package com.jinjinjara.pola.presentation.ui.screen.start

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val isAddBtn: Boolean = false
)

@Composable
fun CategorySelectScreen(
    onCategorySelected: () -> Unit,
) {
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var customCategories by remember { mutableStateOf(listOf<Category>()) }

    val defaultCategories = listOf(
        Category("shopping", "쇼핑", Icons.Outlined.ShoppingBag),
        Category("place", "장소", Icons.Outlined.Place),
        Category("person", "인물", Icons.Outlined.Person),
        Category("snack", "간식", Icons.Outlined.BakeryDining),
        Category("exercise", "운동", Icons.Outlined.FitnessCenter),
        Category("info", "정보", Icons.Outlined.Info),
        Category("study", "학습", Icons.Outlined.School),
        Category("food", "음식", Icons.Outlined.Restaurant),
    )

    val categories = remember(customCategories) {
        defaultCategories + customCategories + Category(
            "add",
            "",
            Icons.Default.Add,
            isAddBtn = true
        )
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
                        color = MaterialTheme.colorScheme.primary, // 원하는 색상
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

        Spacer(modifier = Modifier.height(32.dp))

        // Progress indicator
        Text(
            text = "${selectedCategories.size}/${categories.count { !it.isAddBtn }}",
            fontSize = 16.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        val rowCount = (categories.size + 2) / 3
        // Category Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(36.dp),
            modifier = Modifier
                .weight(1f)  // 남은 공간을 차지
                .verticalScroll(rememberScrollState())
        ) {
            for (rowIndex in 0 until rowCount) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (colIndex in 0..2) {
                        val index = rowIndex * 3 + colIndex
                        if (index < categories.size) {
                            val category = categories[index]
                            CategoryItem(
                                category = category,
                                isSelected = selectedCategories.contains(category.id),
                                onToggle = {
                                    selectedCategories =
                                        if (selectedCategories.contains(category.id)) {
                                            selectedCategories - category.id
                                        } else {
                                            selectedCategories + category.id
                                        }
                                },
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
            onClick = { onCategorySelected() },
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
                customCategories = customCategories + Category(
                    id = "custom_${System.currentTimeMillis()}",
                    name = categoryName,
                    icon = Icons.Outlined.Category
                )
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
        onCategorySelected = {}
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