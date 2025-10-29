package com.jinjinjara.pola.presentation.ui.screen.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier
) {
    // 카테고리 목록과 선택된 카테고리 상태
    val categories = listOf("전체", "여행", "음식", "일상", "친구", "가족")
    var selectedCategory by remember { mutableStateOf("전체") }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Timeline",
                        fontSize = 20.sp,
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: 클릭 이벤트 처리 */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "달력 아이콘",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategoryChips(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                TimelineContent()
            }


//            // 나머지 컨텐츠 영역
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "타임라인 화면",
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
        }
    }
}

@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
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
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else Color.Black
                )
            }
        }
    }
}

@Composable
fun TimelineContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 날짜 & 타임라인 세로선
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(top = 8.dp)

        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .padding(top = 8.dp)
            ) {
                // 원 부분
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = Color(0xFF8B6E4E), // 원 색상
                            shape = RoundedCornerShape(50)
                        )
                )

                // 세로선
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(120.dp)
                        .background(Color(0xFF8B6E4E))
                )
            }


            // 날짜 텍스트
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
            ) {
                Row {
                    Text(
                        text = "25.10.20",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5C4528)
                    )
                    Text(
                        text = "MON",
                        fontSize = 14.sp,
                        color = Color(0xFF8B6E4E)
                    )
                }


                // 여기에 나중에 필름 들어갈 자리
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(width = 300.dp, height = 150.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("필름 자리 (나중에 추가)")
                }
            }
        }
    }
}
