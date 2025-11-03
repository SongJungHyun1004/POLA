package com.jinjinjara.pola.presentation.ui.screen.remind

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindScreen(modifier: Modifier = Modifier) {
    // ▶ 이미지 리스트
    val imageList = listOf(
        "temp_image_1" to R.drawable.temp_image_1,
        "temp_image_2" to R.drawable.temp_image_2,
        "temp_image_3" to R.drawable.temp_image_3,
        "temp_image_4" to R.drawable.temp_image_4,
        "temp_image_1" to R.drawable.temp_image_1,
        "temp_image_2" to R.drawable.temp_image_2,
        "temp_image_3" to R.drawable.temp_image_3,
        "temp_image_4" to R.drawable.temp_image_4
    )

    // ▶ 상태 변수
    var frontIndex by remember { mutableStateOf(0) }
    var displayIndex by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    var animationDirection by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // ▶ 애니메이션 상태
    val frontOffsetX = remember { Animatable(0f) }
    val frontRotationZ = remember { Animatable(0f) }
    val frontAlpha = remember { Animatable(1f) }

    val backOffsetX = remember { Animatable(-20f) }
    val backRotationZ = remember { Animatable(-6f) }
    val backAlpha = remember { Animatable(1f) }

    val nextOffsetX = remember { Animatable(400f) }
    val nextRotationZ = remember { Animatable(6f) }
    val nextAlpha = remember { Animatable(0f) }

    val newBackOffsetX = remember { Animatable(-20f) }
    val newBackRotationZ = remember { Animatable(-6f) }
    val newBackAlpha = remember { Animatable(0f) }

    val backIndex = (frontIndex - 1).coerceAtLeast(0)
    val nextIndex = (frontIndex + 1).coerceAtMost(imageList.lastIndex)
    val newBackIndex = (frontIndex - 2).coerceAtLeast(0)

    // ▶ 다음 이미지 이동
    suspend fun moveToNext() {
        if (isAnimating || frontIndex >= imageList.lastIndex) return
        isAnimating = true
        animationDirection = "left"
        val dur = 520

        displayIndex = (frontIndex + 1).coerceAtMost(imageList.lastIndex)

        scope.launch {
            awaitAll(
                async { frontOffsetX.animateTo(-20f, tween(dur)) },
                async { frontRotationZ.animateTo(-6f, tween(dur)) }
            )
        }

        scope.launch {
            nextAlpha.snapTo(0f)
            awaitAll(
                async { nextAlpha.animateTo(1f, tween(dur)) },
                async { nextOffsetX.animateTo(0f, tween(dur)) },
                async { nextRotationZ.animateTo(0f, tween(dur)) }
            )
        }

        delay(dur.toLong())
        frontIndex += 1

        backOffsetX.snapTo(-20f)
        backRotationZ.snapTo(-6f)
        nextOffsetX.snapTo(400f)
        nextRotationZ.snapTo(6f)
        nextAlpha.snapTo(0f)
        frontOffsetX.snapTo(0f)
        frontRotationZ.snapTo(0f)
        frontAlpha.snapTo(1f)

        isAnimating = false
        animationDirection = ""
    }

    // ▶ 이전 이미지 이동
    suspend fun moveToPrevious() {
        if (isAnimating || frontIndex <= 0) return
        isAnimating = true
        animationDirection = "right"
        val dur = 520

        displayIndex = (frontIndex - 1).coerceAtLeast(0)

        val newBackIdx = (frontIndex - 2).coerceAtLeast(0)
        if (newBackIdx >= 0) {
            newBackOffsetX.snapTo(-20f)
            newBackRotationZ.snapTo(-6f)
            newBackAlpha.snapTo(0f)
            scope.launch { awaitAll(async { newBackAlpha.animateTo(1f, tween(dur)) }) }
        }

        scope.launch {
            awaitAll(
                async { frontOffsetX.animateTo(500f, tween(dur)) },
                async { frontRotationZ.animateTo(6f, tween(dur)) },
                async { frontAlpha.animateTo(0f, tween(dur)) }
            )
        }

        scope.launch {
            awaitAll(
                async { backOffsetX.animateTo(0f, tween(dur)) },
                async { backRotationZ.animateTo(0f, tween(dur)) }
            )
        }

        delay(dur.toLong())
        frontIndex -= 1

        nextOffsetX.snapTo(400f)
        nextRotationZ.snapTo(6f)
        nextAlpha.snapTo(0f)
        frontOffsetX.snapTo(0f)
        frontRotationZ.snapTo(0f)
        frontAlpha.snapTo(1f)
        backOffsetX.snapTo(-20f)
        backRotationZ.snapTo(-6f)
        newBackAlpha.snapTo(0f)

        isAnimating = false
        animationDirection = ""
    }

    // ▶ 썸네일 스크롤
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

    // ▶ UI
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
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount < -30) scope.launch { moveToNext() }
                            else if (dragAmount > 30) scope.launch { moveToPrevious() }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween // 하단 고정
            ) {
                // 상단: 이미지 카드 스택
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {


                    Box(
                        modifier = Modifier.padding(top = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (frontIndex > 1)
                            RemindPolaCard(imageList[newBackIndex].second, newBackOffsetX.value, newBackRotationZ.value, newBackAlpha.value)
                        if (frontIndex > 0)
                            RemindPolaCard(imageList[backIndex].second, backOffsetX.value, backRotationZ.value, backAlpha.value)
                        if (frontIndex < imageList.lastIndex) {
                            if (animationDirection == "left" && isAnimating)
                                FrontThenNextLayer(imageList.map { it.second }, frontIndex, nextIndex, frontOffsetX.value, frontRotationZ.value, frontAlpha.value, nextOffsetX.value, nextRotationZ.value, nextAlpha.value)
                            else
                                NextThenFrontLayer(imageList.map { it.second }, frontIndex, nextIndex, frontOffsetX.value, frontRotationZ.value, frontAlpha.value, nextOffsetX.value, nextRotationZ.value, nextAlpha.value)
                        } else {
                            RemindPolaCard(imageList[frontIndex].second, frontOffsetX.value, frontRotationZ.value, frontAlpha.value)
                        }
                    }
                }

                // 하단: 방향키 + 썸네일 묶음
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 방향키 (Image 사용)
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
                                .clickable(enabled = !isAnimating) { scope.launch { moveToPrevious() } },
                        )
                        Image(
                            painter = painterResource(id = R.drawable.star_primary_solid),
                            contentDescription = "즐겨찾기",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(enabled = !isAnimating) { },
                        )
                        Image(
                            painter = painterResource(id = R.drawable.arrow_right),
                            contentDescription = "다음",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(enabled = !isAnimating) { scope.launch { moveToNext() } },
                        )
                    }

                    // 썸네일 리스트
                    Box(
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .onGloballyPositioned { coords -> viewportWidthPx = coords.size.width.toFloat() }
                    ) {
                        Row(
                            modifier = Modifier.horizontalScroll(scrollState),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Spacer(Modifier.width(8.dp))
                            imageList.forEachIndexed { index, pair ->
                                val isFocused = index == displayIndex
                                Image(
                                    painter = painterResource(id = pair.second),
                                    contentDescription = pair.first,
                                    contentScale = ContentScale.Crop,
                                    colorFilter = if (!isFocused)
                                        ColorFilter.tint(Color.Black.copy(alpha = 0.5f), BlendMode.Multiply)
                                    else null,
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
                                        .clickable {
                                            if (!isAnimating) {
                                                frontIndex = index
                                                displayIndex = index
                                            }
                                        }
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }
}

// PolaCard Wrapper
@Composable
private fun RemindPolaCard(
    imageResId: Int,
    translationX: Float,
    rotationZ: Float,
    alpha: Float
) {
    PolaCard(
        imageResId = imageResId,
        modifier = Modifier
            .height(410.dp)
            .aspectRatio(0.7816f)
            .graphicsLayer {
                this.translationX = translationX
                this.rotationZ = rotationZ
                this.alpha = alpha
            },
        ratio = 0.7523f,
        imageRatio = 0.8352f,
        paddingValues = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp)
    )
}

// 카드 순서 조합
@Composable
private fun NextThenFrontLayer(
    images: List<Int>,
    frontIndex: Int,
    nextIndex: Int,
    frontOffsetX: Float,
    frontRotationZ: Float,
    frontAlpha: Float,
    nextOffsetX: Float,
    nextRotationZ: Float,
    nextAlpha: Float
) {
    RemindPolaCard(images[nextIndex], nextOffsetX, nextRotationZ, nextAlpha)
    RemindPolaCard(images[frontIndex], frontOffsetX, frontRotationZ, frontAlpha)
}

@Composable
private fun FrontThenNextLayer(
    images: List<Int>,
    frontIndex: Int,
    nextIndex: Int,
    frontOffsetX: Float,
    frontRotationZ: Float,
    frontAlpha: Float,
    nextOffsetX: Float,
    nextRotationZ: Float,
    nextAlpha: Float
) {
    RemindPolaCard(images[frontIndex], frontOffsetX, frontRotationZ, frontAlpha)
    RemindPolaCard(images[nextIndex], nextOffsetX, nextRotationZ, nextAlpha)
}
