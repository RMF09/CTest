package com.rmf.ctest.feature.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Range
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.StreamState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.rmf.ctest.R
import com.rmf.ctest.feature.destinations.NoFaceDetectedScreenDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Destination
@Composable
fun CameraScreen(
    isForProfile: Boolean = false,
    resultBackNavigator: ResultBackNavigator<Uri>,
    navigator: DestinationsNavigator
) {

    val activity = LocalContext.current as Activity
    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }
    val outputDirectory = remember {
        activity.getOutputDirectory()
    }
    var shouldShowCamera by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()

    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            cameraExecutor.shutdown()
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                shouldShowCamera = true
            }
        })

    LaunchedEffect(key1 = Unit) {
        activity.requestCameraPermission(
            shouldShowCamera = {
                shouldShowCamera = it
            },
            permissionLaunch = {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            })
    }

    CameraView(
        isForProfile = isForProfile,
        outputDirectory = outputDirectory,
        executor = cameraExecutor,
        navigator = navigator,
        onImageCaptured = {
            cameraExecutor.shutdown()
            scope.launch {
                withContext(Dispatchers.Main) {
                    resultBackNavigator.navigateBack(result = it)
                }
            }
        },
        onError = {
            Toast.makeText(
                activity,
                "Terjadi masalah, harap coba lagi (${it.localizedMessage})",
                Toast.LENGTH_LONG
            ).show()
        }
    )
}

@SuppressLint("RestrictedApi")
@Composable
fun CameraView(
    isForProfile: Boolean,
    outputDirectory: File,
    executor: Executor,
    viewModel: CameraViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // 1
    val lensFacing by remember {
        mutableIntStateOf(
            CameraSelector.LENS_FACING_BACK
        )
    }


    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageSize by remember {
        mutableStateOf(android.util.Size(300, 300))
    }

    val preview = Preview.Builder().setTargetResolution(imageSize).build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture =
        remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setTargetResolution(android.util.Size(previewView.width, previewView.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setImageQueueDepth(10)
            .build()
            .apply {
                setAnalyzer(executor, FaceAnalyzer(
                    object : FaceAnalyzerCallback {
                        override fun processFace(faces: List<Face>) {
                            viewModel.processFace(faces)
                        }

                        override fun errorFace(error: String) {
                        }
                    }
                ))
            }
    }


    var camera: Camera? = remember { null }
    var rangeBrigthness: Range<Int>?

    var min by remember {
        mutableFloatStateOf(0f)
    }

    var max by remember {
        mutableFloatStateOf(0f)
    }

    var shouldShowEV by remember {
        mutableStateOf(false)
    }

    var evValue by remember {
        mutableIntStateOf(0)
    }


    // 2
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
        evValue = 0

        rangeBrigthness = camera?.cameraInfo?.exposureState?.exposureCompensationRange
        min = rangeBrigthness!!.lower.toFloat()
        max = rangeBrigthness!!.upper.toFloat()

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }


    // 3
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Black)
    ) {
        val modifier = Modifier
            .aspectRatio(1f / 1f)
            .align(Alignment.Center)
        AndroidView({ previewView }, modifier = modifier)
        /*Canvas(modifier = Modifier.fillMaxSize()) {
            viewModel.state?.let { rect ->
                val topLeft = Offset(rect.left.toFloat(), rect.top.toFloat())
                val bottomRight = Offset(rect.right.toFloat(), rect.bottom.toFloat())
                val color = Orange // Anda dapat mengganti warna sesuai kebutuhan

                // Gambar kotak pembatas dengan warna dan ketebalan garis yang sesuai
                drawOutline(
                    outline = Outline.Rectangle(Rect(topLeft, bottomRight)),
                    color = color, style = Stroke(width = 8f)
                )
            }
        }*/

        LaunchedEffect(key1 = Unit) {
            if (isForProfile) {
                viewModel.isFrontFaceDetected.collect { result ->
                    if (result)
                        takePhoto(
                            filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                            imageCapture = imageCapture,
                            outputDirectory = outputDirectory,
                            executor = executor,
                            onImageCaptured = onImageCaptured,
                            onError = onError
                        )
                    else
                        navigator.navigate(
                            NoFaceDetectedScreenDestination(
                                message = "Tidak wajah terdeteksi"
                            )
                        )
                }
            } else
                viewModel.isFaceDetected.collect { result ->
                    if (result)
                        takePhoto(
                            filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                            imageCapture = imageCapture,
                            outputDirectory = outputDirectory,
                            executor = executor,
                            onImageCaptured = onImageCaptured,
                            onError = onError
                        )
                    else
                        navigator.navigate(
                            NoFaceDetectedScreenDestination(
                                message = "Tidak wajah terdeteksi"

                            )
                        )
                }
        }

        val showIconButton by previewView.previewStreamState.observeAsState()

        if (showIconButton == StreamState.STREAMING)
            IconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                onClick = {
                    viewModel.performClick()
                },
                content = {
                    Icon(
                        imageVector = Icons.Sharp.Lens,
                        contentDescription = "Take picture",
                        tint = White,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(1.dp)
                            .border(1.dp, White, CircleShape)
                    )
                }
            )

        /*IconButton(
            modifier = Modifier
                .padding(bottom = 20.dp, end = 20.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                lensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_BACK)
                        CameraSelector.LENS_FACING_FRONT
                    else
                        CameraSelector.LENS_FACING_BACK
            },
            content = {
                Icon(
                    imageVector = Icons.Sharp.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = White,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(1.dp)
                )
            }
        )*/

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 8.dp, end = 8.dp, bottom = 76.dp)
                .background(color = Black.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        shouldShowEV = !shouldShowEV
                    }) {
                        Icon(
                            imageVector = Icons.Sharp.Exposure,
                            contentDescription = null,
                            tint = White
                        )
                    }
                }

                //if(shouldShowEV) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        val color = if (evValue == -2) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = -2
                            camera?.cameraControl?.setExposureCompensationIndex(min.toInt())
                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposureNeg2,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                    item {
                        val color = if (evValue == -1) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = -1
                            camera?.cameraControl?.setExposureCompensationIndex(min.toInt() / 2)

                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposureNeg1,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                    item {
                        val color = if (evValue == 0) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = 0
                            camera?.cameraControl?.setExposureCompensationIndex(0)

                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposureZero,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                    item {
                        val color = if (evValue == 1) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = 1
                            camera?.cameraControl?.setExposureCompensationIndex(max.toInt() / 2)
                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposurePlus1,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                    item {
                        val color = if (evValue == 2) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = 2
                            camera?.cameraControl?.setExposureCompensationIndex(max.toInt())

                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposurePlus2,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Activity.requestCameraPermission(
    shouldShowCamera: (Boolean) -> Unit,
    permissionLaunch: () -> Unit
) {
    shouldShowCamera(false)
    when {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED -> {
            shouldShowCamera(true)
        }

        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.CAMERA
        ) ->
            permissionLaunch()

        else ->
            permissionLaunch()

    }
}

private fun takePhoto(
    filenameFormat: String,
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat(
            filenameFormat,
            Locale.getDefault()
        ).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
            onError(exception)
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val savedUri = Uri.fromFile(photoFile)
            onImageCaptured(savedUri)
        }
    })
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private fun Context.getOutputDirectory(): File {
    this.filesDir
    val mediaDir = filesDir.also {
        File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
}

@Composable
fun ComposableLifecycle(
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

private fun analyze(context: Context, detector: FaceDetector, imageUri: Uri) {
    val inputImage = InputImage.fromFilePath(context, imageUri)
    detector.process(inputImage)
        .addOnSuccessListener { faces ->
            faces.forEach { face ->
                val rect = face.boundingBox
                rect.set(
                    rect.left * SCALING_FACTOR,
                    rect.top * (SCALING_FACTOR - 1),
                    rect.right * SCALING_FACTOR,
                    (rect.bottom * SCALING_FACTOR) + 90
                )
            }
        }
        .addOnFailureListener {
            it.printStackTrace()

        }
}

/*private fun cropDetectedFace(){
    Bitmap.crea
}*/

private const val SCALING_FACTOR = 10


class MySnackBarData(override val visuals: SnackbarVisuals) : SnackbarData {
    override fun dismiss() {
        TODO("Not yet implemented")
    }

    override fun performAction() {
        TODO("Not yet implemented")
    }
}

class MySnackbarVisuals(
    override val actionLabel: String?,
    override val message: String,
    override val withDismissAction: Boolean = false,
) : SnackbarVisuals {
    override val duration: SnackbarDuration
        get() = SnackbarDuration.Short
}



