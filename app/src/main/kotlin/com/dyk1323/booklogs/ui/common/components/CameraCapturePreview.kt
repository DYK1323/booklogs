package com.dyk1323.booklogs.ui.common.components

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dyk1323.booklogs.ui.common.theme.BooklogsBodyEmphasisTextStyle
import com.dyk1323.booklogs.ui.common.theme.BooklogsOnOverlay
import com.dyk1323.booklogs.ui.common.theme.BooklogsOverlayScrim

/**
 * "Open the back camera, tap to capture a still bitmap" preview — shared between quote capture
 * (docs/PLAN.md 화면 흐름 #5) and the quick-log sheet's page-number camera button (화면 흐름 #3, "빠른
 * 기록 UX"). Caller owns what happens to the captured [Bitmap] (OCR, cropping, etc.).
 */
@Composable
fun CameraCapturePreview(
    captionText: String,
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
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
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
                .background(BooklogsOverlayScrim)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = captionText, style = BooklogsBodyEmphasisTextStyle, color = BooklogsOnOverlay)
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
