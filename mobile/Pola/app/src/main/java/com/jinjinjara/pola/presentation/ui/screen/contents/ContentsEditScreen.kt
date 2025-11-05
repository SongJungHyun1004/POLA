package com.jinjinjara.pola.presentation.ui.screen.contents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContentsEditScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    var tags by remember { mutableStateOf(listOf("보조배터리", "봉어빵", "컴팩트")) }
    var contentText by remember {
        mutableStateOf("봉어빵 모양의 보조 배터리로, 아기자기하고 독특한 디자인이 특징이며 무게는 약 130g으로 휴대하기에 부담이 없습니다. 한 손에 쏙 들어오는 크기로 주머니나 가방에 넣어 다니기 편리하며, 귀여운 외형 덕분에 실용성과 함께 소장 가치도 높은 제품입니다. 일상 속 작은 포인트 아이템으로, 충전할 때마다 기분까지 만족해지는 매력을 지닙습니다.")
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "수정하기",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "뒤로가기",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onBackClick()
                            }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 태그 섹션
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // 태그 아이콘으로 변경
                    contentDescription = "태그",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "태그",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 태그 리스트
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    EditableTagChip(
                        text = tag,
                        onRemove = {
                            tags = tags.filter { it != tag }
                        }
                    )
                }

                // + 버튼
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(2.dp, CircleShape)
                        .background(Color.Gray, CircleShape)
                        .clickable {
                            // 태그 추가 로직
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "태그 추가",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 내용 섹션
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // 내용 아이콘으로 변경
                    contentDescription = "내용",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "내용",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 내용 입력 필드
            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedTextColor = MaterialTheme.colorScheme.tertiary
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // 저장하기 버튼
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C8060)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "저장하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EditableTagChip(
    text: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .background(
                color = Color(0xFF9C8060),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$text",
                fontSize = 14.sp,
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onRemove()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "태그 제거",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContentsEditScreenPreview() {
    ContentsEditScreen()
}