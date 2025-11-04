package com.jinjinjara.pola.presentation.ui.screen.contents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import com.jinjinjara.pola.presentation.ui.screen.category.CategoryScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentsScreen(
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(231) }
    var isBookmarked by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(true) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "메뉴"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("공유") },
                                onClick = {
                                    onShareClick()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("수정") },
                                onClick = {
                                    onEditClick()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("삭제") },
                                onClick = {
                                    onDeleteClick()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 메인 콘텐츠 카드
            PolaCard(

            )

            Spacer(modifier = Modifier.height(12.dp))

            // 시간 및 출처 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "2시간 전",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    // Chrome 아이콘 표시 (실제로는 이미지 로드)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "C",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4)
                        )
                    }
                }

                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Default.StarBorder,
                    contentDescription = "즐겨찾기",
                    tint = if (isBookmarked) Color(0xFFDAA520) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 해시태그
            val hashtags = listOf(
                "#보조배터리", "#봉어빵", "#컴팩트",
                "#130G", "#작은손", "#5200mAh", "#발열"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(hashtags) { tag ->
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = tag,
                                fontSize = 13.sp
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFB8956A),
                            labelColor = Color.White
                        ),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 접기/펼치기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = if (isExpanded) "접기" else "펼치기",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "접기" else "펼치기",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 설명 텍스트 (접기/펼치기 가능)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "봉어빵 모양의 보조 배터리로, 아기자기하고 독특한 디자인이 특징이며 무게는 약 130g으로 휴대하기에 부담이 없습니다. 한 손에 쏙 들어오는 크기로 주머니나 가방에 넣어 다니기 편리하며, 귀여운 외형 덕분에 실용성과 함",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContentsScreenPreview() {
    ContentsScreen()
}