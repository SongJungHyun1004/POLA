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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
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

    // 각 칩의 실제 너비를 저장
    val chipWidths = remember { mutableStateMapOf<String, Float>() }
    var viewportWidth by remember { mutableStateOf(0f) }

    // 선택된 카테고리로 자동 스크롤 (중앙 정렬)
    LaunchedEffect(selectedCategory, chipWidths.size, viewportWidth) {
        val selectedIndex = categories.indexOf(selectedCategory)
        // 모든 칩의 너비와 뷰포트 너비가 측정된 후 실행
        if (chipWidths.size == categories.size && viewportWidth > 0f) {
            coroutineScope.launch {
                val spacing = with(density) { 8.dp.toPx() }
                val startPadding = with(density) { 8.dp.toPx() }

                // 선택된 칩까지의 누적 너비 계산 (실제 측정값 사용)
                var accumulatedWidth = startPadding
                for (i in 0 until selectedIndex) {
                    val chipWidth = chipWidths[categories[i]] ?: 0f
                    accumulatedWidth += chipWidth + spacing
                }

                // 선택된 칩의 너비
                val selectedChipWidth = chipWidths[selectedCategory] ?: 0f

                // 칩을 화면 중앙에 배치하기 위한 스크롤 위치 계산
                val chipCenter = accumulatedWidth + (selectedChipWidth / 2)
                val viewportCenter = viewportWidth / 2
                val targetScroll = chipCenter - viewportCenter

                // animateScrollTo가 자동으로 0 ~ maxScroll 범위로 제한
                scrollState.animateScrollTo(targetScroll.toInt())
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 12.dp)
            .onGloballyPositioned { coordinates ->
                viewportWidth = coordinates.size.width.toFloat()
            }
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
                    .onGloballyPositioned { coordinates ->
                        // 각 칩의 실제 너비 측정 및 저장
                        chipWidths[category] = coordinates.size.width.toFloat()
                    }
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