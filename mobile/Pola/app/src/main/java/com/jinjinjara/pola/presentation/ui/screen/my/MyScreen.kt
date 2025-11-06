package com.jinjinjara.pola.presentation.ui.screen.my

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userInfoState by viewModel.userInfoState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "마이 화면",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 사용자 정보 표시
            when (val state = userInfoState) {
                is UserInfoUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "사용자 정보 로딩 중...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                is UserInfoUiState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "사용자 정보",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ID: ${state.user.id}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "이메일: ${state.user.email}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "이름: ${state.user.displayName}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "생성일: ${state.user.createdAt}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
                is UserInfoUiState.Error -> {
                    Text(
                        text = "오류: ${state.message}",
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 로그아웃 버튼
            Button(
                onClick = { showLogoutDialog = true },
                enabled = uiState !is MyUiState.LogoutLoading,
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (uiState is MyUiState.LogoutLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "로그아웃",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // 로그아웃 확인 다이얼로그
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "로그아웃",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "정말 로그아웃 하시겠습니까?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text(
                        "확인",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        "취소",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    // 로그아웃 성공 시 자동으로 로그인 화면으로 이동
    LaunchedEffect(uiState) {
        if (uiState is MyUiState.LogoutSuccess) {
            viewModel.resetLogoutState()
        }
    }
}