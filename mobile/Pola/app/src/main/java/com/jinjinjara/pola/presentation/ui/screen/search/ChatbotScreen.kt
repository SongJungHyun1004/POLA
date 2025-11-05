package com.jinjinjara.pola.presentation.ui.screen.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import com.jinjinjara.pola.presentation.ui.component.PolaSearchBar

@Composable
fun ChatbotScreen(
    onBackClick: () -> Unit = {}
) {
    var userInput by remember { mutableStateOf("") }

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
                text = "챗봇",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 채팅 메시지 영역 (스크롤 가능)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 사용자 질문 말풍선
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 60.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "붕어빵 보조배터리 무게가 몇이더라?",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // 챗봇 답변 말풍선
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // 프로필 원형 아이콘 (챗봇)
                    Image(
                        painter = painterResource(id = R.drawable.pola_chatbot),
                        contentDescription = "챗봇 프로필",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )


                    Spacer(Modifier.width(8.dp))

                    Column(
                        modifier = Modifier
                            .padding(end = 60.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "2시간 전 업로드한 이미지에 의하면 붕어빵 모양 보조배터리의 무게는 약 130g 입니다.",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // PolaCard로 이미지 표시
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    // 챗봇 프로필 아이콘과 동일한 간격 유지
                    Spacer(Modifier.width(44.dp)) // 36dp (아이콘) + 8dp (간격)

                    PolaCard(
                        modifier = Modifier
                            .padding(end = 60.dp)
                            .shadow(4.dp, RoundedCornerShape(5.dp)),
                        imageResId = R.drawable.temp_image,
                        textList = listOf("보조배터리", "붕어빵"),
                        textSize = 20.sp,
                        textSpacing = 4.dp,
                        clipTags = true,
                        ratio = 0.7661f,
                        imageRatio = 0.9062f,
                        paddingValues = PaddingValues(
                            top = 12.dp,
                            start = 12.dp,
                            end = 12.dp
                        )
                    )
                }
            }
        }

        // 하단 입력창 (PolaSearchBar 사용)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PolaSearchBar(
                searchText = userInput,
                onValueChange = { userInput = it },
                onSearchClick = {
                    // 전송 로직
                    if (userInput.isNotEmpty()) {
                        // TODO: 메시지 전송 처리
                        userInput = ""
                    }
                },
                iconRes = R.drawable.send,
                placeholder = "메시지를 입력하세요",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
