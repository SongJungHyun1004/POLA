// presentation/ui/screen/my/MyTypeScreen.kt
package com.jinjinjara.pola.presentation.ui.screen.my

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jinjinjara.pola.R
import com.jinjinjara.pola.domain.model.Report

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTypeScreen(
    onBackClick: () -> Unit = {},
    myViewModel: MyViewModel = hiltViewModel(),
    viewModel: MyTypeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val userInfoState by myViewModel.userInfoState.collectAsState()

    val userName = when (userInfoState) {
        is UserInfoUiState.Success -> {
            (userInfoState as UserInfoUiState.Success).user.displayName
        }

        else -> "사용자"
    }
    var showInfoPopup by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "닫기",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                actions = {
                    Box {
                        // Info Icon
                        IconButton(onClick = { showInfoPopup = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "정보",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        // Popup
                        if (showInfoPopup) {
                            Popup(
                                alignment = Alignment.TopEnd,
                                offset = IntOffset(x = -30, y = 100),
                                onDismissRequest = { showInfoPopup = false }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFF444444),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "나만의 수집 방식을 발견해보세요",
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is MyTypeUiState.Loading -> {
                LoadingContent(paddingValues)
            }

            is MyTypeUiState.Success -> {
                SuccessContent(
                    paddingValues = paddingValues,
                    userName = userName,
                    reports = (uiState as MyTypeUiState.Success).reports
                )
            }

            is MyTypeUiState.Error -> {
                ErrorContent(
                    paddingValues = paddingValues,
                    message = (uiState as MyTypeUiState.Error).message,
                    onRetry = { viewModel.retry() }
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SuccessContent(
    paddingValues: PaddingValues,
    userName: String,
    reports: List<Report>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title
            Text(
                text = buildAnnotatedString {
                    append("${userName}님의\n")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("수집 타입")
                    }
                    append("은?")
                },
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 48.dp, start = 24.dp)
            )

        }


        if (reports.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.empty),
                        contentDescription = "No content",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "아직 생성된 리포트가 없습니다.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Collection Type Cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { Spacer(Modifier.width(8.dp)) }

//                repeat(10) {

                items(reports) { report ->
                    ReportCard(report = report)
                }
//                }
                item { Spacer(Modifier.width(8.dp)) }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    paddingValues: PaddingValues,
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}

@Composable
fun ReportCard(
    report: Report,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getBackgroundColorForType(report.reportType)
    val periodText = formatReportWeek(report.reportWeek)

    Column(
        modifier = modifier.width(280.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Period label
        Text(
            text = periodText,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = modifier
                .width(330.dp)
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
            ) {
                if (!report.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = report.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.matchParentSize()
                    )
                }

                // 임시 이미지 테스트
                Image(
                    painter = painterResource(id = getImageForType(report.reportType)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = modifier.fillMaxSize()
                ) {
                    // 공유, 다운로드 아이콘
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { /* TODO: 공유 */ }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp * 0.8f)
                            )
                        }

                        Spacer(Modifier.width(4.dp))

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { /* TODO: 다운로드 */ }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp * 0.8f)
                            )
                        }
                    }
                    // Type name
                    Box(Modifier.padding(start = 20.dp)) {
                        repeat(5) {
                            Text(
                                text = report.title,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Transparent,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.White,
                                        offset = Offset(0f, 0f),
                                        blurRadius = 50f
                                    )
                                )
                            )
                        }

                        Text(
                            text = report.title,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }


                    Spacer(modifier = Modifier.weight(1f))

                    // Description
                    Box(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        // Glow layers (문단 전체에 퍼짐)
                        val glowOffsets = listOf(
                            Offset(0f, 0f),
                            Offset(1f, 1f),
                            Offset(-1f, -1f),
                            Offset(2f, 2f),
                            Offset(-2f, -2f),
                            Offset(0f, 0f),
                            Offset(1f, 1f),
                            Offset(-1f, -1f),
                            Offset(2f, 2f),
                            Offset(-2f, -2f),
                        )

                        glowOffsets.forEach { off ->
                            Text(
                                text = report.description,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = Color.Transparent,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.White,
                                        offset = off,
                                        blurRadius = 50f   // blurRadius 자체는 작게
                                    )
                                )
                            )
                        }

                        // 실제 텍스트 레이어
                        Text(
                            text = report.description,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color.Black
                        )
                    }

                }
            }
        }


    }
}

// Helper Functions
private fun getBackgroundColorForType(reportType: String): Color {
    return when (reportType) {
        "SCREENSHOT_MASTER" -> Color(0xFFE3F2FD)
        "TAG_SCHOLAR" -> Color(0xFFFCE4EC)
        "BOOKMARK_HOARDER" -> Color(0xFFF3E5F5)
        "NIGHT_OWL" -> Color(0xFFE8F5E9)
        "VARIETY_SEEKER" -> Color(0xFFFFF9C4)
        else -> Color(0xFFF5F5F5)
    }
}

private fun getImageForType(reportType: String): Int {
    return when (reportType) {
        "MIRACLE_MORNING_BEAR" -> R.drawable.temp_type1
        "NIGHT_OWL" -> R.drawable.temp_type2
        "OCTOPUS_COLLECTOR" -> R.drawable.temp_type3
        "SCREENSHOT_MASTER" -> R.drawable.temp_type4
        "TRIPITAKA_MASTER" -> R.drawable.temp_type5
        "NO_TYPE" -> R.drawable.temp_type6
        "TAG_ONE_WELL" -> R.drawable.temp_type7
        else -> R.drawable.temp_type6
    }
}

private fun formatReportWeek(reportWeek: String): String {
    // "2025-W03" -> "1월 3주차"
    return try {
        val parts = reportWeek.split("-W")
        if (parts.size == 2) {
            val week = parts[1].toInt()

            // 주차를 월로 변환 (대략적인 계산)
            val month = ((week - 1) / 4) + 1
            val weekOfMonth = ((week - 1) % 4) + 1

            "${month}월 ${weekOfMonth}주차"
        } else {
            reportWeek
        }
    } catch (e: Exception) {
        reportWeek
    }
}