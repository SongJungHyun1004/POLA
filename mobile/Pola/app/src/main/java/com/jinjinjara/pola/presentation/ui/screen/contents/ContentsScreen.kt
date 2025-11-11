package com.jinjinjara.pola.presentation.ui.screen.contents

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jinjinjara.pola.R
import com.jinjinjara.pola.domain.model.FileDetail
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import com.jinjinjara.pola.presentation.ui.screen.category.CategoryScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import kotlin.time.Duration

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContentsScreen(
    fileId: Long,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    viewModel: ContentsViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }

    LaunchedEffect(fileId) {
        viewModel.loadFileDetail(fileId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ContentsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ContentsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadFileDetail(fileId) }) {
                            Text("다시 시도")
                        }
                    }
                }
            }

            is ContentsUiState.Success -> {
                ContentsScreenContent(
                    fileDetail = state.fileDetail,
                    isBookmarked = isBookmarked,
                    showMenu = showMenu,
                    isExpanded = isExpanded,
                    showFullImage = showFullImage,
                    onBackClick = onBackClick,
                    onShareClick = onShareClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    onMenuToggle = { showMenu = !showMenu },
                    onMenuDismiss = { showMenu = false },
                    onExpandToggle = { isExpanded = !isExpanded },
                    onBookmarkToggle = { viewModel.toggleBookmark() },
                    onImageClick = { showFullImage = !showFullImage }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ContentsScreenContent(
    fileDetail: FileDetail,
    isBookmarked: Boolean,
    showMenu: Boolean,
    isExpanded: Boolean,
    showFullImage: Boolean,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMenuToggle: () -> Unit,
    onMenuDismiss: () -> Unit,
    onExpandToggle: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onImageClick: () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(30.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onBackClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "닫기",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    },
                    actions = {
                        Box {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(30.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onMenuToggle()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "메뉴",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            if (showMenu) {
                                Popup(
                                    alignment = Alignment.TopEnd,
                                    onDismissRequest = onMenuDismiss,
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .shadow(12.dp, RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .padding(vertical = 8.dp)
                                    ) {
                                        // 공유 메뉴
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onMenuDismiss()
                                                    onShareClick()
                                                }
                                                .padding(
                                                    start = 16.dp,
                                                    top = 10.dp,
                                                    end = 8.dp,
                                                    bottom = 10.dp
                                                ),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "공유",
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // 수정 메뉴
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onMenuDismiss()
                                                    onEditClick()
                                                }
                                                .padding(
                                                    start = 16.dp,
                                                    top = 10.dp,
                                                    end = 8.dp,
                                                    bottom = 10.dp
                                                ),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "수정",
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // 삭제 메뉴
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onMenuDismiss()
                                                    onDeleteClick()
                                                }
                                                .padding(
                                                    start = 16.dp,
                                                    top = 10.dp,
                                                    end = 8.dp,
                                                    bottom = 10.dp
                                                ),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "삭제",
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                }
                            }
                        }
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
                    .padding(horizontal = 32.dp)
            ) {
                val createdAtMillis = fileDetail.createdAt.atZone(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
                val timeAgo = getTimeAgo(createdAtMillis)
                // 메인 콘텐츠 카드
                PolaCard(
                    modifier = Modifier
                        .shadow(elevation = 8.dp)
                        .clickable { onImageClick() },
                    ratio = 0.7239f,
                    imageRatio = 0.7747f,
                    paddingValues = PaddingValues(
                        top = 14.dp,
                        start = 14.dp,
                        end = 14.dp
                    ),
                    // item.timeAgo
                    timeAgo = timeAgo,
                    // if(item.fromWeb) 크롬 아이콘 else 모바일 아이콘
                    sourceIcon = if (fileDetail.platform.equals("WEB", ignoreCase = true)) {
                        R.drawable.google_chrome_icon
                    } else {
                        R.drawable.mobile_icon
                    },
                    isFavorite = isBookmarked,
                    onFavoriteClick = { onBookmarkToggle() },
                    // item 받아서 넣기
                    imageUrl = fileDetail.src,
                    type = fileDetail.type
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 해시태그
                // item의 해시 태그 값 불러오기
                val hashtags = fileDetail.tags

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = FlowRowOverflow.Clip
                ) {
                    hashtags.forEach { tag ->
                        TagChip(tag.name)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 접기/펼치기 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onExpandToggle()
                            }
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

                // 설명 텍스트
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = fileDetail.context ?: "내용이 없습니다.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )

            }

        }
        // 전체 화면 이미지 뷰어
        if (showFullImage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { onImageClick() }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 뒤로가기 버튼 영역
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "뒤로가기",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onImageClick()
                            }
                    )

                    // 텍스트 파일이면 텍스트 표시
                    if (fileDetail.type?.startsWith("text") == true && !fileDetail.src.isNullOrEmpty()) {
                        var textContent by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(fileDetail.src) {
                            try {
                                textContent = withContext(Dispatchers.IO) {
                                    URL(fileDetail.src).readText(Charsets.UTF_8)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                textContent = "(텍스트 로드 실패)"
                            }
                        }

                        textContent?.let { content ->
                            Text(
                                text = content,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }
                    }

                }
            }
        }
    }
}


fun getTimeAgo(createdAtMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - createdAtMillis

    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)

    return if (hours < 24) {
        when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            else -> "${hours}시간 전"
        }
    } else {
        // 24시간 이상이면 yyyy.MM.dd 형식으로 표시
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        sdf.format(Date(createdAtMillis))
    }
}


@Composable
private fun TagChip(
    text: String,
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$text",
            fontSize = 14.sp,
            color = Color.White,
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ContentsScreenPreview() {
//    ContentsScreen()
//}