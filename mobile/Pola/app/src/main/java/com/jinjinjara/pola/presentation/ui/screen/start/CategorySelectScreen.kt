package com.jinjinjara.pola.presentation.ui.screen.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val isSpecial: Boolean = false
)

@Composable
fun CategorySelectScreen() {
    var selectedCategories by remember { mutableStateOf(setOf("쇼핑")) }

    val categories = listOf(
        Category("shopping", "쇼핑", Icons.Default.ShoppingBag),
        Category("place", "장소", Icons.Default.Place),
        Category("person", "인물", Icons.Default.Person),
        Category("snack", "간식", Icons.Default.Cake),
        Category("exercise", "운동", Icons.Default.FitnessCenter),
        Category("info", "정보", Icons.Default.Info),
        Category("study", "학습", Icons.Default.School),
        Category("food", "음식", Icons.Default.Restaurant),
        Category("add", "", Icons.Default.Add, isSpecial = true)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Title
            Text(
                text = "원하는 카테고리를\n모두 골라주세요",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                color = Color(0xFF2D2D2D)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress indicator
            Text(
                text = "1/10",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Category Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        selectedCategories = if (selectedCategories.contains(category.name)) {
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
                onClick = { /* Navigate to next screen */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA88B6C)
                ),
                shape = RoundedCornerShape(16.dp)
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
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected && !category.isSpecial) {
        Color(0xFFEDE5DA)
    } else {
        Color.White
    }

    val borderColor = if (category.isSpecial) {
        Color(0xFFE0E0E0)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (category.isSpecial) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !category.isSpecial) { onToggle() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    modifier = Modifier.size(if (category.isSpecial) 32.dp else 28.dp),
                    tint = if (category.isSpecial) Color(0xFF666666) else Color(0xFF2D2D2D)
                )
            }

            if (!category.isSpecial) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = category.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D2D2D)
                )
            }
        }

        // Checkmark for selected items
        if (isSelected && !category.isSpecial) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFA88B6C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySelectScreenPreview() {
    CategorySelectScreen()
}