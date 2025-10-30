package com.jinjinjara.pola.presentation.ui.screen.remind

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R
import com.jinjinjara.pola.presentation.ui.component.PolaCard
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindScreen(modifier: Modifier = Modifier) {
    val imageList = listOf(
        "temp_image_1" to R.drawable.temp_image_1,
        "temp_image_2" to R.drawable.temp_image_2,
        "temp_image_3" to R.drawable.temp_image_3,
        "temp_image_4" to R.drawable.temp_image_4
    )

    var frontIndex by remember { mutableStateOf(0) }
    var displayIndex by remember { mutableStateOf(0) } // ✅ 썸네일만 즉시 반응하도록 추가
    var isAnimating by remember { mutableStateOf(false) }
    var animationDirection by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // --- 애니메이션 상태 ---
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

    // -----------------------------
    suspend fun moveToNext() {
        if (isAnimating || frontIndex >= imageList.lastIndex) return
        isAnimating = true
        animationDirection = "left"
        val dur = 520

        displayIndex = (frontIndex + 1).coerceAtMost(imageList.lastIndex) // ✅ 썸네일만 즉시 반응

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
        backAlpha.snapTo(1f)
        nextOffsetX.snapTo(400f)
        nextRotationZ.snapTo(6f)
        nextAlpha.snapTo(0f)
        frontOffsetX.snapTo(0f)
        frontRotationZ.snapTo(0f)
        frontAlpha.snapTo(1f)

        isAnimating = false
        animationDirection = ""
    }

    suspend fun moveToPrevious() {
        if (isAnimating || frontIndex <= 0) return
        isAnimating = true
        animationDirection = "right"
        val dur = 520

        displayIndex = (frontIndex - 1).coerceAtLeast(0) // ✅ 썸네일만 즉시 반응

        val newBackIdx = (frontIndex - 2).coerceAtLeast(0)
        if (newBackIdx >= 0) {
            newBackOffsetX.snapTo(-20f)
            newBackRotationZ.snapTo(-6f)
            newBackAlpha.snapTo(0f)
            scope.launch {
                awaitAll(async { newBackAlpha.animateTo(1f, tween(dur)) })
            }
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
        backAlpha.snapTo(1f)
        newBackOffsetX.snapTo(-20f)
        newBackRotationZ.snapTo(-6f)
        newBackAlpha.snapTo(0f)

        isAnimating = false
        animationDirection = ""
    }

    // -----------------------------
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text("Remind", style = MaterialTheme.typography.titleLarge)
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount < -60) scope.launch { moveToNext() }
                            else if (dragAmount > 60) scope.launch { moveToPrevious() }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = imageList[frontIndex].first,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(contentAlignment = Alignment.Center) {
                    if (frontIndex > 1) {
                        RemindPolaCard(
                            imageResId = imageList[newBackIndex].second,
                            translationX = newBackOffsetX.value,
                            rotationZ = newBackRotationZ.value,
                            alpha = newBackAlpha.value
                        )
                    }

                    if (frontIndex > 0) {
                        RemindPolaCard(
                            imageResId = imageList[backIndex].second,
                            translationX = backOffsetX.value,
                            rotationZ = backRotationZ.value,
                            alpha = backAlpha.value
                        )
                    }

                    if (frontIndex < imageList.lastIndex) {
                        if (animationDirection == "left" && isAnimating) {
                            FrontThenNextLayer(
                                images = imageList.map { it.second },
                                frontIndex = frontIndex,
                                nextIndex = nextIndex,
                                frontOffsetX = frontOffsetX.value,
                                frontRotationZ = frontRotationZ.value,
                                frontAlpha = frontAlpha.value,
                                nextOffsetX = nextOffsetX.value,
                                nextRotationZ = nextRotationZ.value,
                                nextAlpha = nextAlpha.value
                            )
                        } else {
                            NextThenFrontLayer(
                                images = imageList.map { it.second },
                                frontIndex = frontIndex,
                                nextIndex = nextIndex,
                                frontOffsetX = frontOffsetX.value,
                                frontRotationZ = frontRotationZ.value,
                                frontAlpha = frontAlpha.value,
                                nextOffsetX = nextOffsetX.value,
                                nextRotationZ = nextRotationZ.value,
                                nextAlpha = nextAlpha.value
                            )
                        }
                    } else {
                        RemindPolaCard(
                            imageResId = imageList[frontIndex].second,
                            translationX = frontOffsetX.value,
                            rotationZ = frontRotationZ.value,
                            alpha = frontAlpha.value
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(80.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (!isAnimating) scope.launch { moveToPrevious() }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_left),
                            contentDescription = "이전",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!isAnimating) scope.launch { moveToNext() }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_right),
                            contentDescription = "다음",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // ✅ 썸네일 리스트 (방향키 클릭 시 즉시 반응)
                Row(
                    modifier = Modifier
                        .padding(bottom = 36.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.width(8.dp))
                    imageList.forEachIndexed { index, pair ->
                        Image(
                            painter = painterResource(id = pair.second),
                            contentDescription = pair.first,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .graphicsLayer {
                                    alpha = if (index == displayIndex) 1f else 0.4f
                                    scaleX = if (index == displayIndex) 1.05f else 1f
                                    scaleY = if (index == displayIndex) 1.05f else 1f
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

// ✅ PolaCard 래퍼 함수
@Composable
private fun RemindPolaCard(
    imageResId: Int,
    modifier: Modifier = Modifier,
    translationX: Float = 0f,
    rotationZ: Float = 0f,
    alpha: Float = 1f
) {
    PolaCard(
        imageResId = imageResId,
        modifier = modifier
            .height(360.dp)
            .aspectRatio(0.7816f)
            .graphicsLayer {
                this.translationX = translationX
                this.rotationZ = rotationZ
                this.alpha = alpha
            },
        ratio = 0.7816f,
        imageRatio = 0.9152f,
        paddingValues = PaddingValues(top = 8.dp, start = 8.dp, end = 8.dp)
    )
}

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
    RemindPolaCard(
        imageResId = images[nextIndex],
        translationX = nextOffsetX,
        rotationZ = nextRotationZ,
        alpha = nextAlpha
    )

    RemindPolaCard(
        imageResId = images[frontIndex],
        translationX = frontOffsetX,
        rotationZ = frontRotationZ,
        alpha = frontAlpha
    )
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
    RemindPolaCard(
        imageResId = images[frontIndex],
        translationX = frontOffsetX,
        rotationZ = frontRotationZ,
        alpha = frontAlpha
    )

    RemindPolaCard(
        imageResId = images[nextIndex],
        translationX = nextOffsetX,
        rotationZ = nextRotationZ,
        alpha = nextAlpha
    )
}
