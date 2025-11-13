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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jinjinjara.pola.presentation.ui.component.PolaSearchBar

@Composable
fun SearchScreen(
    initialQuery: String = "",
    initialTab: String = "",
    viewModel: SearchViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onSearchClick: (String, String) -> Unit = { _, _ -> }
) {
    var searchText by remember { mutableStateOf(initialQuery) }
    var selectedTab by remember {
        mutableStateOf(
            when (initialTab) {
                "tag" -> "태그 검색"
                "all" -> "통합 검색"
                else -> "태그 검색"
            }
        )
    }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val tagSuggestions by viewModel.tagSuggestions.collectAsState()

    val tabs = listOf("태그 검색", "통합 검색")

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // 초기 검색어가 있을 때 자동 API 호출
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty() && selectedTab == "태그 검색") {
            viewModel.getTagSuggestions(initialQuery)
        }
    }

    // 검색어가 변경될 때마다 API 호출
    LaunchedEffect(searchText, selectedTab) {
        if (selectedTab == "태그 검색" && searchText.isNotEmpty()) {
            viewModel.getTagSuggestions(searchText)
        } else {
            viewModel.clearSuggestions()
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
                    ) { onBackClick() }
            )

            Text(
                text = "검색",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 탭 UI
        TabRow(
            selectedTabIndex = tabs.indexOf(selectedTab),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.tertiary
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == tab)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }

        // 검색바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PolaSearchBar(
                searchText = searchText,
                onValueChange = { searchText = it },
                onSearchClick = {
                    focusManager.clearFocus()
                    val searchType = when (selectedTab) {
                        "태그 검색" -> "tag"
                        "통합 검색" -> "all"
                        else -> "tag"
                    }
                    onSearchClick(searchText, searchType)
                },
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f)
            )
        }
        when (selectedTab) {
            "태그 검색" -> {
                when {
                    searchText.isEmpty() -> {
                        // 아무것도 입력 안 했을 때
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = "태그를 입력해 관련된 컨텐츠를 찾아보세요.",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }

                    tagSuggestions.isEmpty() -> {
                        // 입력했는데 결과 없음
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = "해당 태그가 없습니다.",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }

                    else -> {
                        // 결과 표시
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(tagSuggestions) { tag ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { onTagClick(tag) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "이동",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "통합 검색" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "태그뿐 아니라 컨텐츠 설명까지 함께 검색합니다.",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}