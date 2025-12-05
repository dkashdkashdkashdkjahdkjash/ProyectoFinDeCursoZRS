package com.example.pruebafirebase.ui.utils.methods

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream

// Funcion para comrpobar si el usuario es invitado
fun isGuest(): Boolean {
    val auth = FirebaseAuth.getInstance()
    return auth.currentUser == null
}

// Funcion para generar codigo QR a partir de un String
fun generateQRCode(text: String): ImageBitmap? {
    return try {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(text, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb()
                )
            }
        }
        bmp.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

// Metodo para volver el String base64 en un ImageBitmap
fun decodeBase64ToImageBitmap(base64: String): ImageBitmap? {
    return try {
        val cleanBase64 = base64.substringAfter(",") // por si incluye "data:image/png;base64,"
        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

// Para pasar de uri a base64
fun uriToBase64(context: Context, uri: Uri): String {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source)
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}