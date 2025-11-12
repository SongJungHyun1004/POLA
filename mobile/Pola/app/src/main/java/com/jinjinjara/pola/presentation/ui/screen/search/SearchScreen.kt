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
import com.jinjinjara.pola.presentation.ui.component.PolaSearchBar

@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onSearchClick: (String) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("태그 검색") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val tabs = listOf("태그 검색", "통합 검색")

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // 테스트용 태그 데이터 (검색 시 겹침 많게 구성)
    data class SearchResult(val tag: String)

    val tagResults = listOf(
        // ☕ 카페 관련
        SearchResult("#카페"),
        SearchResult("#스타벅스카페"),
        SearchResult("#예쁜카페"),
        SearchResult("#감성카페"),
        SearchResult("#카페거리"),
        SearchResult("#카페투어"),
        SearchResult("#카페데이트"),
        SearchResult("#카페사진"),
        SearchResult("#카페음악"),
        SearchResult("#카페일상"),
        SearchResult("#카페휴식"),
        SearchResult("#카페라떼"),
        SearchResult("#카페인중독"),
        SearchResult("#카페모카"),
        SearchResult("#카페시간"),

        // 🍰 디저트 & 음식
        SearchResult("#디저트"),
        SearchResult("#디저트카페"),
        SearchResult("#디저트투어"),
        SearchResult("#디저트사진"),
        SearchResult("#케이크"),
        SearchResult("#초코케이크"),
        SearchResult("#딸기케이크"),
        SearchResult("#초코라떼"),
        SearchResult("#딸기라떼"),
        SearchResult("#말차라떼"),
        SearchResult("#커피"),
        SearchResult("#커피타임"),
        SearchResult("#커피한잔"),
        SearchResult("#커피사진"),
        SearchResult("#커피향"),
        SearchResult("#커피와디저트"),
        SearchResult("#커피데이트"),
        SearchResult("#커피브레이크"),

        // 🍜 음식 / 간식
        SearchResult("#떡볶이"),
        SearchResult("#매운떡볶이"),
        SearchResult("#야식"),
        SearchResult("#간식"),
        SearchResult("#간식시간"),
        SearchResult("#분식"),
        SearchResult("#분식집"),
        SearchResult("#치킨"),
        SearchResult("#치킨맥주"),
        SearchResult("#피자"),
        SearchResult("#햄버거"),
        SearchResult("#라면"),
        SearchResult("#컵라면"),
        SearchResult("#편의점라면"),

        // 👬 인물
        SearchResult("#친구"),
        SearchResult("#친구와함께"),
        SearchResult("#베프"),
        SearchResult("#가족"),
        SearchResult("#가족사진"),
        SearchResult("#엄마와딸"),
        SearchResult("#아빠와아들"),
        SearchResult("#동료"),
        SearchResult("#선배"),
        SearchResult("#후배"),
        SearchResult("#연인"),
        SearchResult("#애인"),
        SearchResult("#데이트"),
        SearchResult("#데이트코스"),
        SearchResult("#커플데이트"),

        // 🌇 장소
        SearchResult("#서울"),
        SearchResult("#서울카페"),
        SearchResult("#서울여행"),
        SearchResult("#서울데이트"),
        SearchResult("#한강"),
        SearchResult("#한강공원"),
        SearchResult("#한강야경"),
        SearchResult("#부산"),
        SearchResult("#부산카페"),
        SearchResult("#부산여행"),
        SearchResult("#제주도"),
        SearchResult("#제주카페"),
        SearchResult("#제주여행"),
        SearchResult("#공원"),
        SearchResult("#공원산책"),
        SearchResult("#산책"),
        SearchResult("#야경"),
        SearchResult("#노을"),
        SearchResult("#바다"),
        SearchResult("#바다뷰"),
        SearchResult("#바다카페"),
        SearchResult("#바다여행"),
        SearchResult("#강릉"),
        SearchResult("#강릉카페"),
        SearchResult("#속초"),
        SearchResult("#속초카페"),

        // 🧳 여행 관련
        SearchResult("#여행"),
        SearchResult("#여행사진"),
        SearchResult("#여행일기"),
        SearchResult("#여행기록"),
        SearchResult("#여행준비"),
        SearchResult("#여행스타그램"),
        SearchResult("#국내여행"),
        SearchResult("#해외여행"),
        SearchResult("#가족여행"),
        SearchResult("#커플여행"),
        SearchResult("#혼자여행"),
        SearchResult("#여행중"),
        SearchResult("#여행추억"),

        // 💭 감정
        SearchResult("#행복"),
        SearchResult("#즐거움"),
        SearchResult("#기쁨"),
        SearchResult("#그리움"),
        SearchResult("#외로움"),
        SearchResult("#설렘"),
        SearchResult("#편안함"),
        SearchResult("#여유"),
        SearchResult("#위로"),
        SearchResult("#휴식"),
        SearchResult("#힐링"),
        SearchResult("#힐링카페"),
        SearchResult("#힐링타임"),

        // 📚 일상 & 취미
        SearchResult("#일상"),
        SearchResult("#하루일상"),
        SearchResult("#오늘일기"),
        SearchResult("#기록"),
        SearchResult("#사진기록"),
        SearchResult("#영상기록"),
        SearchResult("#글쓰기"),
        SearchResult("#코딩"),
        SearchResult("#앱개발"),
        SearchResult("#공부"),
        SearchResult("#스터디카페"),
        SearchResult("#스터디"),
        SearchResult("#프로젝트"),
        SearchResult("#회의"),
        SearchResult("#작업중"),
        SearchResult("#야근"),

        // 🎨 취미 & 여가
        SearchResult("#그림"),
        SearchResult("#그림그리기"),
        SearchResult("#사진"),
        SearchResult("#사진찍기"),
        SearchResult("#사진연습"),
        SearchResult("#사진모임"),
        SearchResult("#영화"),
        SearchResult("#영화보기"),
        SearchResult("#음악"),
        SearchResult("#음악감상"),
        SearchResult("#플레이리스트"),
        SearchResult("#게임"),
        SearchResult("#보드게임"),

        // 🌸 계절 & 자연
        SearchResult("#봄"),
        SearchResult("#봄소풍"),
        SearchResult("#벚꽃"),
        SearchResult("#벚꽃길"),
        SearchResult("#여름"),
        SearchResult("#가을"),
        SearchResult("#단풍"),
        SearchResult("#겨울"),
        SearchResult("#눈오는날"),
        SearchResult("#하늘"),
        SearchResult("#구름"),
        SearchResult("#별"),
        SearchResult("#달")
    )

    // 태그 검색 결과 필터링 (한 글자 이상 입력 시)
    val filteredTagResults = remember(searchText) {
        if (searchText.length >= 1) {
            tagResults.filter { it.tag.contains(searchText, ignoreCase = true) }
        } else {
            emptyList()
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
                    onSearchClick(searchText)
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
                                text = "태그를 입력해 관련된 콘텐츠를 찾아보세요.",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }

                    filteredTagResults.isEmpty() -> {
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
                            items(filteredTagResults) { result ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { onTagClick(result.tag) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = result.tag,
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
                        text = "태그뿐 아니라 콘텐츠 설명까지 함께 검색합니다.",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}