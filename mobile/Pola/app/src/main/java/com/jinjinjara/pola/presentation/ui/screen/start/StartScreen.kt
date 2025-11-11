package com.jinjinjara.pola.presentation.ui.screen.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import com.jinjinjara.pola.R
import com.jinjinjara.pola.util.ErrorType

// 로그인 화면
@Composable
fun StartScreen(
    onLoginSuccess: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 에러 토스트 표시
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is StartUiState.Success -> {
                onLoginSuccess(state.onboardingCompleted)
            }
            is StartUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.pola_start),
                contentDescription = "Pola Logo",
                modifier = Modifier.fillMaxWidth()
            )
            // 하단 그라데이션
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.pola_start_title),
            contentDescription = "Pola Logo",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 60.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 에러 메시지 카드
        if (uiState is StartUiState.Error) {
            ErrorMessageCard(
                message = (uiState as StartUiState.Error).message,
                errorType = (uiState as StartUiState.Error).errorType,
                onRetry = {
                    viewModel.resetError()
                    viewModel.signIn(context)
                }
            )
        }

        Spacer(modifier = Modifier.height(if (uiState is StartUiState.Error) 24.dp else 76.dp))

        // 구글 로그인 버튼
        val isLoading = uiState is StartUiState.Loading

        Button(
            onClick = {
                viewModel.signIn(context)
            },
            enabled = !isLoading,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "로그인 중...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google 계정으로 로그인",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    errorType: ErrorType,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3F3)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 에러 아이콘
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 에러 메시지
            Text(
                text = message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD32F2F),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // 에러 타입별 설명
            Spacer(modifier = Modifier.height(8.dp))
            val helpText = when (errorType) {
                ErrorType.NETWORK -> "인터넷 연결을 확인한 후 다시 시도해주세요."
                ErrorType.SERVER -> "서버에 일시적인 문제가 있습니다."
                ErrorType.TIMEOUT -> "요청 시간이 초과되었습니다. 다시 시도해주세요."
                ErrorType.UNAUTHORIZED -> "인증에 실패했습니다."
                ErrorType.GOOGLE_SIGN_IN_CANCELLED -> "로그인이 취소되었습니다."
                ErrorType.GOOGLE_SIGN_IN_FAILED -> "Google 로그인에 실패했습니다."
                else -> "다시 시도해주세요."
            }

            Text(
                text = helpText,
                fontSize = 13.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 다시 시도 버튼
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "다시 시도",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}