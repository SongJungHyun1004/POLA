package com.jinjinjara.pola.presentation.ui.screen.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    var selectedCategories by remember { mutableStateOf(setOf("쇼핑")) }

    val categories = listOf(
        Category("shopping", "쇼핑", Icons.Default.ShoppingBag),
        Category("place", "장소", Icons.Default.Place),
        Category("person", "인물", Icons.Default.Person),
        Category("snack", "간식", Icons.Default.BakeryDining),
        Category("exercise", "운동", Icons.Default.FitnessCenter),
        Category("info", "정보", Icons.Default.Info),
        Category("study", "학습", Icons.Default.School),
        Category("food", "음식", Icons.Default.Restaurant),
        Category("add", "", Icons.Default.Add, isAddBtn = true)
    )


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
            text = "1/10",
            fontSize = 16.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Category Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(36.dp)
        ) {
            for (rowIndex in 0..2) {
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
                                isSelected = selectedCategories.contains(category.name),
                                onToggle = {
                                    selectedCategories =
                                        if (selectedCategories.contains(category.name)) {
                                            selectedCategories - category.name
                                        } else {
                                            selectedCategories + category.name
                                        }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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

}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onToggle: () -> Unit,
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
            .clickable(enabled = !category.isAddBtn) { onToggle() }
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
                    fontWeight = FontWeight.Medium,
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

@Preview(showBackground = true)
@Composable
fun CategorySelectScreenPreview() {
    CategorySelectScreen(
        onCategorySelected = {}
    )
}