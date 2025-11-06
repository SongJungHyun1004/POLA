package com.jinjinjara.pola.presentation.ui.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.PolaSearchBar
import com.jinjinjara.pola.presentation.ui.screen.timeline.CategoryChips

@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onSearchClick: (String) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("전체") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // 화면 진입 시 검색창에 자동 포커스
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val categories = listOf("전체", "장소", "인물", "간식", "쇼핑", "일상", "여행")

    // 카테고리별 테스트 데이터
    data class SearchResult(val tag: String, val count: String, val category: String)

    val allResults = listOf(
        // 장소
        SearchResult("#카페", "24개", "장소"),
        SearchResult("#스타벅스", "18개", "장소"),
        SearchResult("#공원", "15개", "장소"),
        SearchResult("#서울", "32개", "장소"),
        SearchResult("#제주도", "28개", "장소"),
        SearchResult("#바다", "12개", "장소"),
        // 인물
        SearchResult("#친구", "45개", "인물"),
        SearchResult("#가족", "38개", "인물"),
        SearchResult("#동료", "22개", "인물"),
        SearchResult("#선배", "16개", "인물"),
        SearchResult("#후배", "13개", "인물"),
        // 간식
        SearchResult("#말차", "15개", "간식"),
        SearchResult("#말차라떼", "10개", "간식"),
        SearchResult("#말차과자", "9개", "간식"),
        SearchResult("#초코", "25개", "간식"),
        SearchResult("#딸기", "20개", "간식"),
        SearchResult("#케이크", "18개", "간식"),
        SearchResult("#아이스크림", "22개", "간식"),
        SearchResult("#커피", "35개", "간식")
    )

    // 카테고리 및 검색어 필터링
    val filteredResults = remember(selectedCategory, searchText) {
        val categoryFiltered = if (selectedCategory == "전체") {
            allResults
        } else {
            allResults.filter { it.category == selectedCategory }
        }

        // 검색어로 필터링
        if (searchText.isEmpty()) {
            categoryFiltered
        } else {
            categoryFiltered.filter {
                it.tag.contains(searchText, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 상단 앱바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "뒤로가기",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.CenterStart)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onBackClick()
                    }
            )

            Text(
                text = "검색",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 검색바
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
                onSearchClick = {
                    // 키보드 내리기
                    focusManager.clearFocus()
                    // 돋보기 버튼 클릭 시 이벤트
                    onSearchClick(searchText)
                },
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f)
            )
        }

        // 카테고리 칩 (CategoryScreen 스타일 재사용)
        CategoryChips(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(Modifier.height(20.dp))

        // 검색 결과 리스트
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredResults) { result ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTagClick(result.tag) }
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = result.tag,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = result.count,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "이동",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
