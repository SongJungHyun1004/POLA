package com.jinjinjara.pola.presentation.ui.screen.remind

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindScreen(
    modifier: Modifier = Modifier,
    onNavigateToContents: (Long) -> Unit = {},
    viewModel: RemindViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 에러 토스트 처리
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // UI 상태에 따른 분기
    when (val state = uiState) {
        is RemindUiState.Loading -> {
            Log.d(TAG, "UI State: Loading")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        is RemindUiState.Error -> {
            Log.e(TAG, "UI State: Error - ${state.message}")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadReminders() }) {
                        Text("다시 시도")
                    }
                }
            }
            return
        }

        is RemindUiState.Success -> {
            val imageList = state.data
            Log.d(TAG, "UI State: Success - 이미지 개수: ${imageList.size}")
            imageList.forEachIndexed { index, remindData ->
                Log.d(TAG, "  [$index] id: ${remindData.id}, imageUrl: ${remindData.imageUrl}")
            }

            if (imageList.isEmpty()) {
                Log.w(TAG, "이미지 리스트가 비어있음")
                Scaffold(
                    modifier = modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0.dp),
                    topBar = {
                        TopAppBar(
                            title = {
                                Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Remind",
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            },
                            windowInsets = WindowInsets(0.dp)
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
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
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "리마인드 할 컨텐츠가 없어요",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                return
            }

            RemindScreenContent(
                modifier = modifier,
                imageList = imageList,
                onNavigateToContents = onNavigateToContents,
                viewModel = viewModel
            )
        }
    }
}

private const val TAG = "RemindScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindScreenContent(
    modifier: Modifier = Modifier,
    imageList: List<com.jinjinjara.pola.domain.model.RemindData>,
    onNavigateToContents: (Long) -> Unit = {},
    viewModel: RemindViewModel
) {
    Log.d(TAG, "RemindScreenContent 시작 - 이미지 개수: ${imageList.size}")

    // 상태 변수
    var frontIndex by remember { mutableStateOf(0) }
    var displayIndex by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    var animationDirection by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // 애니메이션 상태
    val frontOffsetX = remember { Animatable(0f) }
    val frontRotationZ = remember { Animatable(0f) }
    val frontAlpha = remember { Animatable(1f) }

    val backOffsetX = remember { Animatable(20f) }
    val backRotationZ = remember { Animatable(6f) }
    val backAlpha = remember { Animatable(1f) }

    val back2OffsetX = remember { Animatable(20f) }
    val back2RotationZ = remember { Animatable(6f) }
    val back2Alpha = remember { Animatable(0f) }

    val prevOffsetX = remember { Animatable(-400f) }
    val prevRotationZ = remember { Animatable(-6f) }
    val prevAlpha = remember { Animatable(0f) }

    val backIndex = (frontIndex + 1).coerceAtMost(imageList.lastIndex)
    val back2Index = (frontIndex + 2).coerceAtMost(imageList.lastIndex)
    val prevIndex = (frontIndex - 1).coerceAtLeast(0)

    // frontIndex 변경 시 back2Alpha 조정
    LaunchedEffect(frontIndex) {
        if (!isAnimating) {
            if (frontIndex < imageList.lastIndex - 1) {
                back2Alpha.animateTo(1f, tween(300))
            } else {
                back2Alpha.animateTo(0f, tween(300))
            }
        }
    }

    // 다음 이미지 이동
    suspend fun moveToNext() {
        if (isAnimating || frontIndex >= imageList.lastIndex) return
        isAnimating = true
        animationDirection = "left"
        val dur = 520

        displayIndex = (frontIndex + 1).coerceAtMost(imageList.lastIndex)

        // 새로운 back2 페이드인
        val newFrontIndex = frontIndex + 1
        val needsNewBack2 = newFrontIndex < imageList.lastIndex - 1
        if (needsNewBack2) {
            scope.launch {
                back2Alpha.animateTo(1f, tween(dur))
            }
        }

        // 왼쪽으로 사라지는 애니메이션
        scope.launch {
            awaitAll(
                async { frontOffsetX.animateTo(-500f, tween(dur)) },
                async { frontRotationZ.animateTo(-6f, tween(dur)) },
                async { frontAlpha.animateTo(0f, tween(dur)) }
            )
        }

        // 앞으로 이동하는 애니메이션
        scope.launch {
            awaitAll(
                async { backOffsetX.animateTo(0f, tween(dur)) },
                async { backRotationZ.animateTo(0f, tween(dur)) }
            )
        }

        delay(dur.toLong())
        frontIndex += 1

        // 상태 리셋
        backOffsetX.snapTo(20f)
        backRotationZ.snapTo(6f)
        back2OffsetX.snapTo(20f)
        back2RotationZ.snapTo(6f)
        frontOffsetX.snapTo(0f)
        frontRotationZ.snapTo(0f)
        frontAlpha.snapTo(1f)

        isAnimating = false
        animationDirection = ""
    }

    // 이전 이미지 이동
    suspend fun moveToPrevious() {
        if (isAnimating || frontIndex <= 0) return
        isAnimating = true
        animationDirection = "right"
        val dur = 520

        displayIndex = (frontIndex - 1).coerceAtLeast(0)

        // 오른쪽 뒤로 이동하는 애니메이션
        scope.launch {
            awaitAll(
                async { frontOffsetX.animateTo(20f, tween(dur)) },
                async { frontRotationZ.animateTo(6f, tween(dur)) }
            )
        }

        // 왼쪽에서 등장하는 애니메이션
        scope.launch {
            prevAlpha.snapTo(0f)
            prevOffsetX.snapTo(-400f)
            prevRotationZ.snapTo(-6f)
            awaitAll(
                async { prevAlpha.animateTo(1f, tween(dur)) },
                async { prevOffsetX.animateTo(0f, tween(dur)) },
                async { prevRotationZ.animateTo(0f, tween(dur)) }
            )
        }

        delay(dur.toLong())
        frontIndex -= 1

        // 상태 리셋
        prevOffsetX.snapTo(-400f)
        prevRotationZ.snapTo(-6f)
        prevAlpha.snapTo(0f)
        frontOffsetX.snapTo(0f)
        frontRotationZ.snapTo(0f)
        frontAlpha.snapTo(1f)
        backOffsetX.snapTo(20f)
        backRotationZ.snapTo(6f)
        back2OffsetX.snapTo(20f)
        back2RotationZ.snapTo(6f)

        isAnimating = false
        animationDirection = ""
    }

    // 썸네일 스크롤
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    var viewportWidthPx by remember { mutableStateOf(0f) }

    LaunchedEffect(displayIndex, viewportWidthPx) {
        if (viewportWidthPx == 0f) return@LaunchedEffect
        with(density) {
            val thumbnailWidthPx = 80.dp.toPx() + 12.dp.toPx()
            val targetStart = thumbnailWidthPx * displayIndex
            val targetEnd = targetStart + thumbnailWidthPx
            val visibleStart = scrollState.value.toFloat()
            val visibleEnd = visibleStart + viewportWidthPx

            if (targetStart < visibleStart) {
                scrollState.animateScrollTo((targetStart - 12.dp.toPx()).toInt().coerceAtLeast(0))
            } else if (targetEnd > visibleEnd) {
                val newScroll = (targetEnd - viewportWidthPx + 42.dp.toPx()).toInt()
                scrollState.animateScrollTo(newScroll.coerceAtMost(scrollState.maxValue))
            }
        }
    }

    // UI
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Remind",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )

                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .graphicsLayer {
                    clip = false
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = false
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount < -30) scope.launch { moveToNext() }
                            else if (dragAmount > 30) scope.launch { moveToPrevious() }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 이미지 카드 스택
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            clip = false
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {


                    Box(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .graphicsLayer {
                                clip = false
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        // 뒤쪽 카드들
                        if (frontIndex < imageList.lastIndex - 1 && back2Alpha.value > 0f)
                            RemindPolaCard(
                                imageUrl = imageList[back2Index].imageUrl,
                                tags = imageList[back2Index].tags,
                                translationX = back2OffsetX.value,
                                rotationZ = back2RotationZ.value,
                                alpha = 1f
                            )
                        if (frontIndex < imageList.lastIndex)
                            RemindPolaCard(
                                imageUrl = imageList[backIndex].imageUrl,
                                tags = imageList[backIndex].tags,
                                translationX = backOffsetX.value,
                                rotationZ = backRotationZ.value,
                                alpha = backAlpha.value
                            )

                        // 전면 카드 레이어
                        if (frontIndex > 0) {
                            if (animationDirection == "right" && isAnimating)
                                PrevThenFrontLayer(
                                    imageList = imageList,
                                    frontIndex = frontIndex,
                                    prevIndex = prevIndex,
                                    frontOffsetX = frontOffsetX.value,
                                    frontRotationZ = frontRotationZ.value,
                                    frontAlpha = frontAlpha.value,
                                    prevOffsetX = prevOffsetX.value,
                                    prevRotationZ = prevRotationZ.value,
                                    prevAlpha = prevAlpha.value
                                )
                            else
                                FrontThenPrevLayer(
                                    imageList = imageList,
                                    frontIndex = frontIndex,
                                    prevIndex = prevIndex,
                                    frontOffsetX = frontOffsetX.value,
                                    frontRotationZ = frontRotationZ.value,
                                    frontAlpha = frontAlpha.value,
                                    prevOffsetX = prevOffsetX.value,
                                    prevRotationZ = prevRotationZ.value,
                                    prevAlpha = prevAlpha.value
                                )
                        } else {
                            RemindPolaCard(
                                onNavigateToContents = { onNavigateToContents(imageList[frontIndex].id) },
                                imageUrl = imageList[frontIndex].imageUrl,
                                tags = imageList[frontIndex].tags,
                                translationX = frontOffsetX.value,
                                rotationZ = frontRotationZ.value,
                                alpha = frontAlpha.value
                            )
                        }
                    }
                }

                // 하단 컨트롤
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // 썸네일 리스트
                    Box(
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .onGloballyPositioned { coords ->
                                viewportWidthPx = coords.size.width.toFloat()
                            }
                    ) {
                        Row(
                            modifier = Modifier.horizontalScroll(scrollState),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Spacer(Modifier.width(8.dp))
                            imageList.forEachIndexed { index, remindData ->
                                val isFocused = index == displayIndex
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .graphicsLayer {
                                            alpha = if (isFocused) 1f else 0.8f
                                            scaleX = if (isFocused) 1.05f else 1f
                                            scaleY = if (isFocused) 1.05f else 1f
                                        }
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            if (!isAnimating) {
                                                frontIndex = index
                                                displayIndex = index
                                            }
                                        }
                                ) {
                                    AsyncImage(
                                        model = remindData.imageUrl,
                                        contentDescription = "Remind ${remindData.id}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (!isFocused) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.5f))
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                    }

                    // 방향키
                    Row(
                        modifier = Modifier.padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(70.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.arrow_left),
                            contentDescription = "이전",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(
                                    enabled = !isAnimating,
                                    indication = ripple(bounded = false, radius = 24.dp),
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { scope.launch { moveToPrevious() } },
                        )
                        Image(
                            painter = painterResource(
                                id = if (imageList[displayIndex].isFavorite)
                                    R.drawable.star_primary_solid
                                else
                                    R.drawable.star_primary
                            ),
                            contentDescription = "즐겨찾기",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(
                                    enabled = !isAnimating,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    viewModel.toggleFavorite(displayIndex)
                                },
                        )
                        Image(
                            painter = painterResource(id = R.drawable.arrow_right),
                            contentDescription = "다음",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(
                                    enabled = !isAnimating,
                                    indication = ripple(bounded = false, radius = 24.dp),
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { scope.launch { moveToNext() } },
                        )
                    }
                }
            }
        }
    }
}

// PolaCard Wrapper
@Composable
private fun RemindPolaCard(
    onNavigateToContents: () -> Unit = {},
    imageUrl: String,
    tags: List<String>,
    translationX: Float,
    rotationZ: Float,
    alpha: Float
) {
    val context = LocalContext.current

    PolaCard(
        imageUrl = imageUrl,
        textList = tags.map { it.removePrefix("#") },
        textSize = 24.sp,
        textSpacing = 8.dp,
        clipTags = true,
        modifier = Modifier
            .height(410.dp)
            .aspectRatio(0.7816f)
            .graphicsLayer {
                this.translationX = translationX
                this.rotationZ = rotationZ
                this.alpha = alpha
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onNavigateToContents()
            },
        ratio = 0.7523f,
        imageRatio = 0.8352f,
        paddingValues = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp)
    )
}

// 카드 순서 조합
@Composable
private fun FrontThenPrevLayer(
    imageList: List<com.jinjinjara.pola.domain.model.RemindData>,
    frontIndex: Int,
    prevIndex: Int,
    frontOffsetX: Float,
    frontRotationZ: Float,
    frontAlpha: Float,
    prevOffsetX: Float,
    prevRotationZ: Float,
    prevAlpha: Float
) {
    RemindPolaCard(
        imageUrl = imageList[prevIndex].imageUrl,
        tags = imageList[prevIndex].tags,
        translationX = prevOffsetX,
        rotationZ = prevRotationZ,
        alpha = prevAlpha
    )
    RemindPolaCard(
        imageUrl = imageList[frontIndex].imageUrl,
        tags = imageList[frontIndex].tags,
        translationX = frontOffsetX,
        rotationZ = frontRotationZ,
        alpha = frontAlpha
    )
}

@Composable
private fun PrevThenFrontLayer(
    imageList: List<com.jinjinjara.pola.domain.model.RemindData>,
    frontIndex: Int,
    prevIndex: Int,
    frontOffsetX: Float,
    frontRotationZ: Float,
    frontAlpha: Float,
    prevOffsetX: Float,
    prevRotationZ: Float,
    prevAlpha: Float
) {
    RemindPolaCard(
        imageUrl = imageList[frontIndex].imageUrl,
        tags = imageList[frontIndex].tags,
        translationX = frontOffsetX,
        rotationZ = frontRotationZ,
        alpha = frontAlpha
    )
    RemindPolaCard(
        imageUrl = imageList[prevIndex].imageUrl,
        tags = imageList[prevIndex].tags,
        translationX = prevOffsetX,
        rotationZ = prevRotationZ,
        alpha = prevAlpha
    )
}

