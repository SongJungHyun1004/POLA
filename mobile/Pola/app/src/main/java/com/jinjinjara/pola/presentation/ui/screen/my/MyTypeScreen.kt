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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jinjinjara.pola.presentation.ui.screen.home.HomeScreen

data class CollectionType(
    val period: String,
    val typeName: String,
    val description: String,
    val backgroundColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTypeScreen(
    userName: String = "OO",
    onBackClick: () -> Unit = {}
) {
    val collectionTypes = listOf(
        CollectionType(
            period = "10월 2주차",
            typeName = "태그한우물",
            description = "당신은 관심 분야가\n확고한 사람이에요.",
            backgroundColor = Color(0xFFE3F2FD)
        ),
        CollectionType(
            period = "10월 1주차",
            typeName = "스크린샷 장인",
            description = "당신은 관심 분야가\n확고한 사람이에요.",
            backgroundColor = Color(0xFFFCE4EC)
        )
    )

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
                        append("수집 타입은?")
                    }
                },
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Collection Type Cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { Spacer(Modifier.width(8.dp)) }
                items(collectionTypes) { type ->
                    CollectionTypeCard(type = type)
                }
            }
        }
    }
}

@Composable
fun CollectionTypeCard(
    type: CollectionType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .height(420.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = type.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Period label
            Text(
                text = type.period,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Type name
            Text(
                text = type.typeName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color.Black
                )
            }

            // Description
            Text(
                text = type.description,
                fontSize = 16.sp,
                color = Color.Black,
                lineHeight = 24.sp,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyTypeScreenPreview() {
    MaterialTheme {
        MyTypeScreen()
    }
}