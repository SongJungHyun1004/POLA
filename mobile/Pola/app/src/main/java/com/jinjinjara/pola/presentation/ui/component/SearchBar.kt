package com.jinjinjara.pola.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.R

@Composable
fun SearchBar(
    searchText: String,
    onSearchClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAiMode by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            color = MaterialTheme.colorScheme.tertiary,
            width = 2.dp
        ),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI 버튼
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isAiMode)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isAiMode = !isAiMode
                    },
            ) {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // 검색 영역
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onSearchClick(isAiMode)
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                val placeholder = if (isAiMode)
                    "챗봇과 대화해보세요."
                else
                    "검색어를 입력하세요."

                Text(
                    text = if (searchText.isEmpty()) placeholder else searchText,
                    color = if (searchText.isEmpty()) Color.Gray else Color.Black,
                    fontSize = 16.sp,
                )
            }

            // 아이콘 전환
            val iconRes = if (isAiMode) R.drawable.send else R.drawable.search

            Icon(
                painter = painterResource(id = iconRes),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = if (isAiMode) "send" else "search",
                modifier = Modifier
                    .width(25.dp)
                    .fillMaxHeight()
            )
        }
    }
}
