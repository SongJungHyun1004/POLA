package com.jinjinjara.pola.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jinjinjara.pola.R

@Composable
fun PolaCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    imageResId: Int? = null,
    ratio: Float = 3f / 4f,
    paddingValues: PaddingValues = PaddingValues(
        top = 32.dp,
        start = 32.dp,
        end = 32.dp
    ),
    borderColor: Color = MaterialTheme.colorScheme.tertiary,
    content: @Composable (() -> Unit)? = null
) {
    // 전체 폴라로이드 카드
    Card(
        modifier = modifier
            .aspectRatio(ratio)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                RoundedCornerShape(5.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(5.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            // 내부 사진 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(5.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                val painter = painterResource(
                    id = imageResId ?: R.drawable.temp_image
                )
                Image(
                    painter = painter,
                    contentDescription = "Content",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(5.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            // 정보 영역

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PolaCardPreview() {
    PolaCard() {
        // 미리보기용 빈 콘텐츠
    }
}