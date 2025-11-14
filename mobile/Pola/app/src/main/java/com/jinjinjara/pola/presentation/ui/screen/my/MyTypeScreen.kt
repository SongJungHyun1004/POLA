// presentation/ui/screen/my/MyTypeScreen.kt
package com.jinjinjara.pola.presentation.ui.screen.my

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jinjinjara.pola.domain.model.Report
import com.jinjinjara.pola.presentation.viewmodel.MyTypeUiState
import com.jinjinjara.pola.presentation.viewmodel.MyTypeViewModel

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
                            contentDescription = "닫기"
                        )
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
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp, start = 24.dp)
        )

        if (reports.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "아직 생성된 리포트가 없습니다.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Collection Type Cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { Spacer(Modifier.width(8.dp)) }

                repeat(10) {

                    items(reports) { report ->
                        ReportCard(report = report)
                    }
                }
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
        modifier = Modifier
            .fillMaxSize(),

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
                .width(280.dp)
                .height(420.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {


            // Type name
            Text(
                text = report.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(20.dp)
            )

            // Icon or Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!report.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = report.imageUrl,
                        contentDescription = report.title,
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = Color.Black
                    )
                }
            }

            // Description
            Text(
                text = report.description,
                fontSize = 16.sp,
                color = Color.Black,
                lineHeight = 24.sp,
                modifier = Modifier.padding(20.dp)
            )
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