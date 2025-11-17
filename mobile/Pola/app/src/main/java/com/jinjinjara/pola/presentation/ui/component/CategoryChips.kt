package com.jinjinjara.pola.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 선택된 카테고리로 자동 스크롤
    LaunchedEffect(selectedCategory) {
        val selectedIndex = categories.indexOf(selectedCategory)
        // 화면에 이미 보이는 처음 2-3개 항목은 스크롤하지 않음
        if (selectedIndex > 2) {
            coroutineScope.launch {
                // 각 칩의 평균 너비 추정 (텍스트 + padding + spacing)
                // 2글자 카테고리 기준: 한글 2글자(~40dp) + padding(36dp) ≈ 70dp
                val estimatedChipWidth = with(density) { 70.dp.toPx() }
                val spacing = with(density) { 8.dp.toPx() }
                val startPadding = with(density) { 8.dp.toPx() }

                // 선택된 칩의 대략적인 위치 계산
                val scrollPosition = (selectedIndex * (estimatedChipWidth + spacing)) - startPadding

                scrollState.animateScrollTo(scrollPosition.toInt())
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 12.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.width(8.dp))
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                shadowElevation = if (isSelected) 4.dp else 2.dp,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onCategorySelected(category) }
            ) {
                Text(
                    text = category,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.tertiary
                )
            }
        }
        Spacer(Modifier.width(16.dp))
    }
}