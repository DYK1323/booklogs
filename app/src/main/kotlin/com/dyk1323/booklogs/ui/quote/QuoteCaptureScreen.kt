package com.dyk1323.booklogs.ui.quote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dyk1323.booklogs.ui.common.components.CameraCapturePreview
import com.dyk1323.booklogs.ui.common.components.LoadingOverlay

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
        imageBounds = null
        imageContainerSize = IntSize.Zero
    }

    fun onPhotoReady(bitmap: Bitmap) {
        capturedBitmap = bitmap
        imageBounds = null
        viewModel.prefillPageNumber(bitmap)
        viewModel.recognizeFullPage(bitmap)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) loadBitmapFromUri(context, uri)?.let(::onPhotoReady)
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
                actions = {
                    if (capturedBitmap != null && uiState.editingPageIndex == null) {
                        TextButton(
                            onClick = {
                                viewModel.discardCurrentCaptureText()
                                openCameraForNextCapture()
                            },
                        ) {
                            Text(text = "다시 촬영")
                        }
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
                uiState.editingPageIndex != null -> FinalTextContent(
                    state = uiState,
                    onPageChanged = viewModel::updatePageText,
                    onQuoteChanged = viewModel::updateQuoteText,
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
                capturedBitmap != null -> WordSelectPhotoContent(
                    bitmap = capturedBitmap!!,
                    state = uiState,
                    imageBounds = imageBounds,
                    imageContainerSize = imageContainerSize,
                    onContainerSizeChanged = { size ->
                        imageContainerSize = size
                        imageBounds = computeImageBounds(size, capturedBitmap!!)
                    },
                    onWordTap = viewModel::selectWord,
                    onGapToggle = viewModel::toggleLineBreakGap,
                    onCancelSelection = viewModel::cancelSelection,
                    onConfirmSelection = viewModel::confirmSelection,
                )
                else -> Box(modifier = Modifier.fillMaxSize()) {
                    if (hasCameraPermission) {
                        CameraCapturePreview(
                            captionText = "페이지를 맞춘 뒤 먼저 사진을 찍어주세요.",
                            onCaptured = ::onPhotoReady,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        PermissionMessage()
                    }
                    TextButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "앨범에서 선택", color = Color.White)
                    }
                }
            }
            if (uiState.isRecognizing) {
                LoadingOverlay(message = "텍스트 인식 중…")
            }
        }
    }
}

/** Decodes a gallery-picked image, honoring EXIF orientation (handled automatically by ImageDecoder). */
private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, false)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}.getOrNull()

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

/** Step 2/3: full-screen photo for word-range tap selection, gap-adjustment sheet overlaid on top. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordSelectPhotoContent(
    bitmap: Bitmap,
    state: QuoteCaptureUiState,
    imageBounds: ComposeRect?,
    imageContainerSize: IntSize,
    onContainerSizeChanged: (IntSize) -> Unit,
    onWordTap: (Int) -> Unit,
    onGapToggle: (Int) -> Unit,
    onCancelSelection: () -> Unit,
    onConfirmSelection: () -> Unit,
) {
    val startIndex = state.selectionStartIndex
    val endIndex = state.selectionEndIndex

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = when {
                startIndex == null -> "시작 단어를 터치해주세요."
                endIndex == null -> "끝 단어를 터치해주세요."
                else -> "다른 단어를 탭하면 범위를 다시 고를 수 있어요."
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .onSizeChanged(onContainerSizeChanged)
                .pointerInput(bitmap, imageBounds, state.recognizedWords) {
                    detectTapGestures(
                        onTap = { offset ->
                            val bounds = imageBounds ?: return@detectTapGestures
                            val bitmapPoint = offset.toBitmapPoint(bounds, bitmap)
                            val index = state.recognizedWords.nearestWordIndex(bitmapPoint)
                            if (index != null) onWordTap(index)
                        },
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
                state.recognizedWords.forEachIndexed { index, word ->
                    val viewRect = word.boundingBox.toComposeRect(bounds, bitmap) ?: return@forEachIndexed
                    val selected = when {
                        startIndex != null && endIndex != null ->
                            index in minOf(startIndex, endIndex)..maxOf(startIndex, endIndex)
                        startIndex != null -> index == startIndex
                        else -> false
                    }
                    if (selected) {
                        drawRect(
                            color = Color(0xFFFFD54F).copy(alpha = 0.42f),
                            topLeft = Offset(viewRect.left, viewRect.top),
                            size = androidx.compose.ui.geometry.Size(viewRect.width, viewRect.height),
                        )
                    }
                    drawLine(
                        color = if (selected) Color(0xFFFFC107) else Color.Black.copy(alpha = 0.4f),
                        start = Offset(viewRect.left, viewRect.bottom),
                        end = Offset(viewRect.right, viewRect.bottom),
                        strokeWidth = 2.dp.toPx(),
                    )
                }
            }
        }
    }

    if (startIndex != null && endIndex != null) {
        ModalBottomSheet(onDismissRequest = onCancelSelection) {
            GapAdjustmentSheetContent(
                words = state.recognizedWords,
                startIndex = startIndex,
                endIndex = endIndex,
                mergedLineBreakGaps = state.mergedLineBreakGaps,
                onGapToggle = onGapToggle,
                onReselect = onCancelSelection,
                onConfirm = onConfirmSelection,
            )
        }
    }
}

/** Step 4: shown once a range has been confirmed for the current photo — no photo here, just review/save. */
@Composable
private fun FinalTextContent(
    state: QuoteCaptureUiState,
    onPageChanged: (String) -> Unit,
    onQuoteChanged: (String) -> Unit,
    onNextPage: () -> Unit,
    onContinueAfterSave: () -> Unit,
    onSave: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        CapturedPagesSummary(pages = state.capturedPages)
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text(text = "최종 인용구") },
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
                TextButton(onClick = onNextPage) {
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

/** "제거할 공백을 터치하세요" step: the selected range shown as flowing text, gap pairs tappable. */
@Composable
private fun GapAdjustmentSheetContent(
    words: List<RecognizedWord>,
    startIndex: Int,
    endIndex: Int,
    mergedLineBreakGaps: Set<Int>,
    onGapToggle: (Int) -> Unit,
    onReselect: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "제거할 공백을 터치하세요.", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onReselect) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "단어 다시 선택하기")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        GapAdjustableText(
            words = words,
            startIndex = startIndex,
            endIndex = endIndex,
            mergedLineBreakGaps = mergedLineBreakGaps,
            onGapToggle = onGapToggle,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(text = "사용하기")
        }
    }
}

/**
 * Renders the selected words as flowing prose. Ordinary words are plain text; each line-break gap is
 * rendered as ONE shared highlighted unit covering both flanking words (matching how the space actually
 * reads), and tapping that unit toggles whether the space between them is kept or removed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GapAdjustableText(
    words: List<RecognizedWord>,
    startIndex: Int,
    endIndex: Int,
    mergedLineBreakGaps: Set<Int>,
    onGapToggle: (Int) -> Unit,
) {
    val from = minOf(startIndex, endIndex).coerceIn(words.indices)
    val to = maxOf(startIndex, endIndex).coerceIn(words.indices)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        var i = from
        while (i <= to) {
            val hasGapAfter = i < to && words[i].lineId != words[i + 1].lineId
            if (hasGapAfter) {
                val gapIndex = i
                val merged = gapIndex in mergedLineBreakGaps
                val pairText = if (merged) {
                    words[i].text + words[i + 1].text
                } else {
                    "${words[i].text} ${words[i + 1].text}"
                }
                val containerColor = if (merged) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
                val contentColor = if (merged) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
                Text(
                    text = pairText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    modifier = Modifier
                        .background(containerColor, RoundedCornerShape(4.dp))
                        .clickable { onGapToggle(gapIndex) }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
                i += 2
            } else {
                Text(text = words[i].text, style = MaterialTheme.typography.bodyLarge)
                i += 1
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

/** Bitmap-space word box (from ML Kit) -> view-space rect within the displayed (letterboxed) image. */
private fun Rect.toComposeRect(imageBounds: ComposeRect, bitmap: Bitmap): ComposeRect? {
    if (imageBounds.width <= 0f || imageBounds.height <= 0f || bitmap.width <= 0 || bitmap.height <= 0) return null
    val scaleX = imageBounds.width / bitmap.width
    val scaleY = imageBounds.height / bitmap.height
    return ComposeRect(
        left = imageBounds.left + left * scaleX,
        top = imageBounds.top + top * scaleY,
        right = imageBounds.left + right * scaleX,
        bottom = imageBounds.top + bottom * scaleY,
    )
}

/** View-space tap -> bitmap-space point, clamped into the displayed image. */
private fun Offset.toBitmapPoint(imageBounds: ComposeRect, bitmap: Bitmap): Offset {
    val clampedX = x.coerceIn(imageBounds.left, imageBounds.right)
    val clampedY = y.coerceIn(imageBounds.top, imageBounds.bottom)
    val scaleX = if (imageBounds.width > 0f) bitmap.width / imageBounds.width else 1f
    val scaleY = if (imageBounds.height > 0f) bitmap.height / imageBounds.height else 1f
    return Offset((clampedX - imageBounds.left) * scaleX, (clampedY - imageBounds.top) * scaleY)
}

/** Nearest word to a bitmap-space point: containment wins, otherwise the closest by center distance. */
private fun List<RecognizedWord>.nearestWordIndex(point: Offset): Int? {
    if (isEmpty()) return null
    val contained = indexOfFirst { it.boundingBox.contains(point.x.toInt(), point.y.toInt()) }
    if (contained >= 0) return contained
    return indices.minByOrNull { i ->
        val box = this[i].boundingBox
        val centerX = (box.left + box.right) / 2f
        val centerY = (box.top + box.bottom) / 2f
        val dx = point.x - centerX
        val dy = point.y - centerY
        dx * dx + dy * dy
    }
}
