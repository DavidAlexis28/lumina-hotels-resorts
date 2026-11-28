package com.lumina.luminahotels.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min

object ImageUploadHelper {
    suspend fun compressAndPrepareImage(context: Context, uri: Uri): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val allowedMimes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp")
            if (!allowedMimes.any { mimeType.contains(it, ignoreCase = true) }) {
                throw IllegalArgumentException("Formato no compatible ($mimeType). Solo se permiten imágenes JPG, PNG o WEBP.")
            }

            contentResolver.openInputStream(uri)?.use { stream ->
                val originalBitmap = BitmapFactory.decodeStream(stream)
                    ?: throw IllegalArgumentException("No se pudo procesar la imagen seleccionada.")

                val maxDimension = 1600
                val width = originalBitmap.width
                val height = originalBitmap.height
                val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                    val ratio = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        (width * ratio).toInt(),
                        (height * ratio).toInt(),
                        true
                    )
                } else originalBitmap

                val bos = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
                val bytes = bos.toByteArray()
                if (bytes.size > 5 * 1024 * 1024) {
                    throw IllegalArgumentException("La imagen excede el tamaño máximo permitido de 5 MB.")
                }
                bytes
            } ?: throw IllegalStateException("No se pudo abrir el archivo de imagen.")
        }
    }
}

/**
 * Selector de foto desde la galería para Alojamientos y Atracciones con subida real a Supabase Storage.
 * Exclusivamente selector de galería (sin permisos de cámara).
 */
@Composable
fun CatalogImagePicker(
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    onUploadImageBytes: suspend (ByteArray) -> Result<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var localPreviewUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            localPreviewUri = uri
            errorMessage = null
            isUploading = true
            coroutineScope.launch {
                val prepResult = ImageUploadHelper.compressAndPrepareImage(context, uri)
                prepResult.onSuccess { bytes ->
                    val uploadResult = onUploadImageBytes(bytes)
                    uploadResult.onSuccess { publicUrl ->
                        onImageUrlChange(publicUrl)
                        isUploading = false
                    }.onFailure { err ->
                        errorMessage = err.message ?: "Error al subir foto a Supabase Storage."
                        isUploading = false
                    }
                }.onFailure { err ->
                    errorMessage = err.message ?: "Error al validar la foto seleccionada."
                    isUploading = false
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Fotografía de catálogo (Supabase Storage):",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )

        val hasImage = imageUrl.isNotBlank() || localPreviewUri != null

        if (hasImage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = localPreviewUri ?: imageUrl,
                    contentDescription = "Vista previa de foto",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                            Text(
                                text = "Subiendo foto a Supabase Storage...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isUploading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cambiar foto", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = {
                        localPreviewUri = null
                        onImageUrlChange("")
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isUploading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC92A3E))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quitar foto", style = MaterialTheme.typography.labelMedium)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .clickable(enabled = !isUploading) {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Subiendo foto a Supabase Storage...",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Seleccionar foto",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Elegir foto desde la galería",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "JPG, PNG o WEBP (Máx. 5 MB)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        errorMessage?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
