package com.jinjinjara.pola.presentation.ui.screen.upload

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun UploadScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onClipboardClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onImagesSelected: (List<GalleryImage>) -> Unit = {},
    onUploadSuccess: () -> Unit = {},
    viewModel: UploadViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // 권한 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadGalleryImages(context)
        }
    }

    // 권한 확인 및 이미지 로드
    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permission)
    }

    // 업로드 상태 처리
    LaunchedEffect(uiState.uploadState) {
        when (val state = uiState.uploadState) {
            is UploadScreenState.Success -> {
                Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetUploadState()
                viewModel.clearSelection()
                onUploadSuccess()
            }
            is UploadScreenState.Error -> {
                Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetUploadState()
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // TopAppBar
        Surface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
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

                Text(
                    text = "Upload",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary
                )

                // 선택된 이미지가 있을 때만 완료 버튼 표시
                if (uiState.selectedImage != null) {
                    TextButton(
                        onClick = {
                            viewModel.uploadSelectedImage(context)
                        },
                        modifier = Modifier.align(Alignment.CenterEnd),
                        enabled = uiState.uploadState !is UploadScreenState.Uploading
                    ) {
                        Text(
                            text = if (uiState.uploadState is UploadScreenState.Uploading) "업로드 중..." else "업로드",
                            fontSize = 14.sp,
                            color = if (uiState.uploadState is UploadScreenState.Uploading)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "클립보드",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onClipboardClick()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Grid
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // 카메라 아이콘 셀 (첫 번째)
                item {
                    CameraCell(onClick = onCameraClick)
                }

                // 갤러리 이미지들
                items(
                    items = uiState.images,
                    key = { it.id }
                ) { image ->
                    PhotoCell(
                        image = image,
                        onImageClick = {
                            viewModel.selectImage(image)
                        }
                    )
                }
            }
        }

        // 업로드 중 오버레이
        if (uiState.uploadState is UploadScreenState.Uploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "업로드 중...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CameraCell(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .background(Color(0xFFC7C7C7))
            .border(width = 0.5.dp, color = Color.Black)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoCamera,
            contentDescription = "사진 촬영",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun PhotoCell(
    image: GalleryImage,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .border(width = 0.5.dp, color = Color.Black)
            .clickable(onClick = onImageClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.uri)
                .crossfade(true)
                .build(),
            contentDescription = image.displayName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 선택 표시
        if (image.isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "선택됨",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyPhotoCell(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .background(Color(0xFFC7C7C7))
            .border(width = 0.5.dp, color = Color.Black)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UploadScreenPreview() {
    MaterialTheme {
        UploadScreen()
    }
}