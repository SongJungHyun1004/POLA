package com.jinjinjara.pola.presentation.ui.screen.chatbot

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen() {
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateOf(listOf("Hello! How can I help you?")) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pola Chatbot") })
        },
        bottomBar = {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your message") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            reverseLayout = true
        ) {
            items(messages.value.reversed()) { msg ->
                Text(text = msg, modifier = Modifier.padding())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatbotScreenPreview() {
    ChatbotScreen()
}
