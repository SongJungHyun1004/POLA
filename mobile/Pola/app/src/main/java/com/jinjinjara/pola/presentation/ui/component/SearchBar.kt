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
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(40.dp),
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
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 검색 영역
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, end = 12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onSearchClick()
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (searchText.isEmpty()) "검색어를 입력하세요." else searchText,
                    color = if (searchText.isEmpty()) Color.Gray else Color.Black,
                    fontSize = 16.sp,
                )
            }

            // 검색 아이콘
            Icon(
                painter = painterResource(id = R.drawable.search),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "search",
                modifier = Modifier
                    .width(25.dp)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onSearchClick()
                    }
            )
        }
    }
}
