package com.dyk1323.booklogs.ui.quote

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteCaptureScreen(
    bookId: Long,
    viewModel: QuoteCaptureViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(bookId) {
        viewModel.start(bookId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var highlightRect by remember { mutableStateOf<ComposeRect?>(null) }
    var imageBounds by remember { mutableStateOf<ComposeRect?>(null) }
    var imageContainerSize by remember { mutableStateOf(IntSize.Zero) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun openCameraForNextCapture() {
        capturedBitmap = null
        highlightRect = null
        imageBounds = null
        imageContainerSize = IntSize.Zero
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "인용구 촬영") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            when {
                !hasCameraPermission -> PermissionMessage()
                capturedBitmap == null -> QuoteCameraPreview(
                    onCaptured = { bitmap ->
                        capturedBitmap = bitmap
                        highlightRect = null
                        imageBounds = null
                        viewModel.prefillPageNumber(bitmap)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                else -> HighlightQuoteContent(
                    bitmap = capturedBitmap!!,
                    state = uiState,
                    highlightRect = highlightRect,
                    imageBounds = imageBounds,
                    imageContainerSize = imageContainerSize,
                    onContainerSizeChanged = { size ->
                        imageContainerSize = size
                        imageBounds = computeImageBounds(size, capturedBitmap!!)
                    },
                    onHighlightChanged = { highlightRect = it },
                    onPageChanged = viewModel::updatePageText,
                    onQuoteChanged = viewModel::updateQuoteText,
                    onRecognize = {
                        val cropRect = highlightRect?.toBitmapRect(
                            imageBounds = imageBounds,
                            bitmap = capturedBitmap!!,
                        )
                        if (cropRect == null) {
                            viewModel.updateQuoteText("")
                        } else {
                            viewModel.recognize(capturedBitmap!!, cropRect)
                        }
                    },
                    onRetake = {
                        viewModel.discardCurrentCaptureText()
                        openCameraForNextCapture()
                    },
                    onNextPage = {
                        viewModel.startNextPage()
                        openCameraForNextCapture()
                    },
                    onContinueAfterSave = {
                        viewModel.beginNewQuote()
                        openCameraForNextCapture()
                    },
                    onSave = viewModel::save,
                    onDone = onBack,
                )
            }
        }
    }
}

@Composable
private fun PermissionMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "책 페이지를 촬영하려면 카메라 권한이 필요해요.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun QuoteCameraPreview(
    onCaptured: (Bitmap) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            cameraProvider.unbindAll()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { view ->
                    view.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = view
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener(
                        {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(view.surfaceProvider)
                            }
                            runCatching {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                )
                            }
                        },
                        ContextCompat.getMainExecutor(ctx),
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.56f))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "페이지를 맞춘 뒤 먼저 사진을 찍어주세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    previewView?.bitmap
                        ?.copy(Bitmap.Config.ARGB_8888, false)
                        ?.let(onCaptured)
                },
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "촬영")
            }
        }
    }
}

@Composable
private fun HighlightQuoteContent(
    bitmap: Bitmap,
    state: QuoteCaptureUiState,
    highlightRect: ComposeRect?,
    imageBounds: ComposeRect?,
    imageContainerSize: IntSize,
    onContainerSizeChanged: (IntSize) -> Unit,
    onHighlightChanged: (ComposeRect) -> Unit,
    onPageChanged: (String) -> Unit,
    onQuoteChanged: (String) -> Unit,
    onRecognize: () -> Unit,
    onRetake: () -> Unit,
    onNextPage: () -> Unit,
    onContinueAfterSave: () -> Unit,
    onSave: () -> Unit,
    onDone: () -> Unit,
) {
    var dragStart by remember(bitmap) { mutableStateOf<Offset?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(text = "인용할 문구 위를 손으로 드래그해 표시하세요.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .onSizeChanged(onContainerSizeChanged)
                .pointerInput(bitmap, imageBounds) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val bounds = imageBounds ?: return@detectDragGestures
                            val start = offset.coerceIn(bounds)
                            dragStart = start
                            onHighlightChanged(ComposeRect(start, start))
                        },
                        onDrag = { change, _ ->
                            val bounds = imageBounds ?: return@detectDragGestures
                            val start = dragStart ?: change.position.coerceIn(bounds)
                            val end = change.position.coerceIn(bounds)
                            onHighlightChanged(ComposeRect(start, end).normalized())
                        },
                        onDragEnd = { dragStart = null },
                        onDragCancel = { dragStart = null },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val bounds = imageBounds ?: computeImageBounds(imageContainerSize, bitmap)
                drawRect(
                    color = Color.Black.copy(alpha = 0.18f),
                    topLeft = Offset(bounds.left, bounds.top),
                    size = androidx.compose.ui.geometry.Size(bounds.width, bounds.height),
                )
                highlightRect?.let { rect ->
                    drawRect(
                        color = Color(0xFFFFD54F).copy(alpha = 0.36f),
                        topLeft = Offset(rect.left, rect.top),
                        size = androidx.compose.ui.geometry.Size(rect.width, rect.height),
                    )
                    drawRect(
                        color = Color(0xFFFFC107),
                        topLeft = Offset(rect.left, rect.top),
                        size = androidx.compose.ui.geometry.Size(rect.width, rect.height),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onRetake) {
                Text(text = "다시 촬영")
            }
            Button(
                onClick = onRecognize,
                enabled = !state.isRecognizing && highlightRect != null && !state.isSaved,
                shape = RoundedCornerShape(8.dp),
            ) {
                if (state.isRecognizing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "텍스트 인식")
                }
            }
        }
        if (state.capturedPages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            CapturedPagesSummary(pages = state.capturedPages)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = state.currentPageText,
            onValueChange = onPageChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "현재 페이지") },
            singleLine = true,
            enabled = !state.isSaved,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.quoteText,
            onValueChange = onQuoteChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "최종 인용구") },
            minLines = 2,
            maxLines = 4,
            enabled = !state.isSaved,
        )
        state.message?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = if (state.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (state.isSaved) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onContinueAfterSave) {
                    Text(text = "계속 촬영")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDone, shape = RoundedCornerShape(8.dp)) {
                    Text(text = "완료")
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onNextPage, enabled = state.capturedPages.isNotEmpty()) {
                    Text(text = "다음 페이지 이어서 촬영")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSave, enabled = !state.isSaving, shape = RoundedCornerShape(8.dp)) {
                    Text(text = if (state.isSaving) "저장 중" else "저장")
                }
            }
        }
    }
}

@Composable
private fun CapturedPagesSummary(pages: List<CapturedQuotePage>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        pages.forEach { page ->
            val pageLabel = page.pageText.ifBlank { "?" }
            Text(
                text = "${page.order}페이지 그룹 · p. $pageLabel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
            )
        }
    }
}

private fun computeImageBounds(containerSize: IntSize, bitmap: Bitmap): ComposeRect {
    if (containerSize.width == 0 || containerSize.height == 0) return ComposeRect.Zero
    val containerRatio = containerSize.width.toFloat() / containerSize.height.toFloat()
    val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

    return if (bitmapRatio > containerRatio) {
        val width = containerSize.width.toFloat()
        val height = width / bitmapRatio
        val top = (containerSize.height - height) / 2f
        ComposeRect(0f, top, width, top + height)
    } else {
        val height = containerSize.height.toFloat()
        val width = height * bitmapRatio
        val left = (containerSize.width - width) / 2f
        ComposeRect(left, 0f, left + width, height)
    }
}

private fun ComposeRect.toBitmapRect(imageBounds: ComposeRect?, bitmap: Bitmap): Rect? {
    val bounds = imageBounds ?: return null
    val clipped = intersect(bounds).normalized()
    if (clipped.width < 12f || clipped.height < 12f) return null

    val scaleX = bitmap.width / bounds.width
    val scaleY = bitmap.height / bounds.height
    val left = ((clipped.left - bounds.left) * scaleX).toInt().coerceIn(0, bitmap.width - 1)
    val top = ((clipped.top - bounds.top) * scaleY).toInt().coerceIn(0, bitmap.height - 1)
    val right = ((clipped.right - bounds.left) * scaleX).toInt().coerceIn(left + 1, bitmap.width)
    val bottom = ((clipped.bottom - bounds.top) * scaleY).toInt().coerceIn(top + 1, bitmap.height)
    return Rect(left, top, right, bottom)
}

private fun ComposeRect.normalized(): ComposeRect =
    ComposeRect(
        left = minOf(left, right),
        top = minOf(top, bottom),
        right = maxOf(left, right),
        bottom = maxOf(top, bottom),
    )

private fun Offset.coerceIn(bounds: ComposeRect): Offset =
    Offset(
        x = x.coerceIn(bounds.left, bounds.right),
        y = y.coerceIn(bounds.top, bounds.bottom),
    )
