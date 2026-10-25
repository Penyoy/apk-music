package com.sonicplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.sonicplayer.ui.theme.Ink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The active artwork, heavily blurred and desaturated as a background wash.
 *
 * The blur is done in-app so it also works on API 24-30. Callers should pass
 * [com.sonicplayer.artwork.ArtworkProvider.rawSongUri] so a missing track artwork
 * leaves the screen solid black instead of blurring the placeholder tile.
 */
@Composable
fun AmbientArtwork(
    artwork: Any?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var blurred by remember(artwork) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(artwork) {
        blurred = null
        if (artwork == null) return@LaunchedEffect
        val bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(context)
                    .data(artwork)
                    .size(256)
                    .allowHardware(false)
                    .build()
                val result = context.imageLoader.execute(request)
                val drawable = (result as? SuccessResult)?.drawable
                    ?: return@runCatching null
                blurBitmap(drawable.toBitmap())
            }.getOrNull()
        }
        blurred = bitmap?.asImageBitmap()
    }

    Box(modifier = modifier.background(Ink)) {
        blurred?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.42f),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0.42f) }
                )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Ink.copy(alpha = 0.32f),
                        0.45f to Ink.copy(alpha = 0.68f),
                        0.78f to Ink.copy(alpha = 0.88f),
                        1f to Ink.copy(alpha = 0.97f)
                    )
                )
        )
    }
}

private fun blurBitmap(source: Bitmap): Bitmap {
    val width = (source.width / 4).coerceAtLeast(1)
    val height = (source.height / 4).coerceAtLeast(1)
    val bitmap = Bitmap.createScaledBitmap(source, width, height, true)
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    boxBlur(pixels, width, height, 12)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}

private fun boxBlur(pixels: IntArray, width: Int, height: Int, radius: Int) {
    val safeRadius = radius.coerceIn(1, maxOf(1, minOf(width, height) / 2))
    val temp = IntArray(pixels.size)
    blurHorizontal(pixels, temp, width, height, safeRadius)
    blurVertical(temp, pixels, width, height, safeRadius)
}

private fun blurHorizontal(
    src: IntArray,
    dst: IntArray,
    width: Int,
    height: Int,
    radius: Int
) {
    val diameter = radius * 2 + 1
    for (y in 0 until height) {
        var sumA = 0
        var sumR = 0
        var sumG = 0
        var sumB = 0
        for (offset in -radius..radius) {
            val pixel = src[y * width + offset.coerceIn(0, width - 1)]
            sumA += pixel ushr 24 and 0xFF
            sumR += pixel ushr 16 and 0xFF
            sumG += pixel ushr 8 and 0xFF
            sumB += pixel and 0xFF
        }
        for (x in 0 until width) {
            dst[y * width + x] =
                (sumA / diameter shl 24) or
                    (sumR / diameter shl 16) or
                    (sumG / diameter shl 8) or
                    (sumB / diameter)

            val outgoing = src[y * width + (x - radius).coerceIn(0, width - 1)]
            val incoming = src[y * width + (x + radius + 1).coerceIn(0, width - 1)]
            sumA += (incoming ushr 24 and 0xFF) - (outgoing ushr 24 and 0xFF)
            sumR += (incoming ushr 16 and 0xFF) - (outgoing ushr 16 and 0xFF)
            sumG += (incoming ushr 8 and 0xFF) - (outgoing ushr 8 and 0xFF)
            sumB += (incoming and 0xFF) - (outgoing and 0xFF)
        }
    }
}

private fun blurVertical(
    src: IntArray,
    dst: IntArray,
    width: Int,
    height: Int,
    radius: Int
) {
    val diameter = radius * 2 + 1
    for (x in 0 until width) {
        var sumA = 0
        var sumR = 0
        var sumG = 0
        var sumB = 0
        for (offset in -radius..radius) {
            val pixel = src[offset.coerceIn(0, height - 1) * width + x]
            sumA += pixel ushr 24 and 0xFF
            sumR += pixel ushr 16 and 0xFF
            sumG += pixel ushr 8 and 0xFF
            sumB += pixel and 0xFF
        }
        for (y in 0 until height) {
            dst[y * width + x] =
                (sumA / diameter shl 24) or
                    (sumR / diameter shl 16) or
                    (sumG / diameter shl 8) or
                    (sumB / diameter)

            val outgoing = src[(y - radius).coerceIn(0, height - 1) * width + x]
            val incoming = src[(y + radius + 1).coerceIn(0, height - 1) * width + x]
            sumA += (incoming ushr 24 and 0xFF) - (outgoing ushr 24 and 0xFF)
            sumR += (incoming ushr 16 and 0xFF) - (outgoing ushr 16 and 0xFF)
            sumG += (incoming ushr 8 and 0xFF) - (outgoing ushr 8 and 0xFF)
            sumB += (incoming and 0xFF) - (outgoing and 0xFF)
        }
    }
}
