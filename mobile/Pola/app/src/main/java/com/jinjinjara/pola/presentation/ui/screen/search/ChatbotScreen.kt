package com.jinjinjara.pola.presentation.ui.screen.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import com.jinjinjara.pola.presentation.ui.component.PolaSearchBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatbotScreen(
    viewModel: ChatbotViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    var userInput by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // messages 변경 시 스크롤을 맨 아래로
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
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
                text = "AI 도우미",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 채팅 메시지 영역 (스크롤 가능)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages.size) { index ->
                val message = messages[index]
                when (message) {
                    is ChatMessage.User -> {
                        // 사용자 메시지
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 70.dp)
                                    .shadow(4.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = message.text,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    is ChatMessage.BotLoading -> {
                        // 로딩 메시지
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
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
                                    .padding(end = 30.dp)
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "생각 중입니다...",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    is ChatMessage.Bot -> {
                        // 챗봇 답변
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
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
                                    .padding(end = 30.dp)
                                    .shadow(4.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = message.text,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    is ChatMessage.BotImage -> {
                        // PolaCard 이미지 (URL) - 단일
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Spacer(Modifier.width(44.dp))

                            PolaCard(
                                modifier = Modifier
                                    .padding(end = 30.dp)
                                    .shadow(4.dp, RoundedCornerShape(5.dp)),
                                imageUrl = message.imageUrl,
                                textList = message.tags,
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

                    is ChatMessage.BotImageGrid -> {
                        // PolaCard 이미지 그리드 (2열)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 2개씩 묶어서 Row로 표시
                            message.images.chunked(2).forEach { rowImages ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Spacer(Modifier.width(44.dp))

                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 30.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowImages.forEach { imageData ->
                                            PolaCard(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .shadow(4.dp, RoundedCornerShape(5.dp)),
                                                imageUrl = imageData.imageUrl,
                                                textList = imageData.tags,
                                                textSize = 14.sp,
                                                textSpacing = 3.dp,
                                                clipTags = true,
                                                ratio = 0.7661f,
                                                imageRatio = 0.9062f,
                                                paddingValues = PaddingValues(
                                                    top = 8.dp,
                                                    start = 8.dp,
                                                    end = 8.dp
                                                )
                                            )
                                        }

                                        // 홀수개일 경우 빈 공간 추가
                                        if (rowImages.size == 1) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 상단 경계선
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
        )

        // 하단 입력창 (PolaSearchBar 사용)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PolaSearchBar(
                searchText = userInput,
                onValueChange = { userInput = it },
                onSearchClick = {
                    if (userInput.isNotEmpty()) {
                        val messageText = userInput

                        // 키보드 내리기
                        focusManager.clearFocus()

                        // ViewModel을 통해 메시지 추가 및 검색 실행
                        viewModel.addUserMessage(messageText)
                        viewModel.addLoadingMessage()
                        viewModel.search(messageText)

                        // 입력창 초기화
                        userInput = ""
                    }
                },
                iconRes = R.drawable.send,
                placeholder = "메시지를 입력하세요.",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
