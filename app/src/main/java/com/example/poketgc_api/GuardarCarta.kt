package com.example.poketgc_api

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.poketgc_api.Datos.Data.PokeCardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object GuardarCarta {

    suspend fun savePokemonImage(context: Context, pokeCardData: PokeCardData) {
        val imageUrl = "${pokeCardData.imagen}/high.png"
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false) // Necesario para obtener el bitmap
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
            if (bitmap != null) {
                val fileName = "${pokeCardData.nombre?.replace(" ", "_") ?: "pokemon"}_${pokeCardData.id}.jpg"
                val saved = saveBitmapToGallery(context, bitmap, fileName)
                withContext(Dispatchers.Main) {
                    if (saved) {
                        Toast.makeText(context, "Carta guardada en galería (PokeCards)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al guardar la carta", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean {
        val albumName = "PokeCards"
        val outputStream: OutputStream?
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$albumName")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val albumDir = File(imagesDir, albumName)
                if (!albumDir.exists()) {
                    albumDir.mkdirs()
                }
                val file = File(albumDir, fileName)
                outputStream = FileOutputStream(file)
            }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
