package com.example.modeloPrueba

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanIntentResult
import android.widget.Toast
import com.example.pruebafirebase.CustomCaptureActivity

class QRScannerActivity : ComponentActivity() {

    // Registrar el launcher para recibir resultados
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            // Código escaneado
            Toast.makeText(this, "QR: ${result.contents}", Toast.LENGTH_LONG).show()
            // Aquí podrías enviar el resultado a otra pantalla si quieres
        } else {
            Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }
        finish() // cerrar la Activity después de escanear
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startScan()
    }

    private fun startScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Escanea el QR")
            setBeepEnabled(true)
            setOrientationLocked(true) // bloquea solo la orientación de la cámara a vertical
            setCaptureActivity(CustomCaptureActivity::class.java)
        }
        barcodeLauncher.launch(options)
    }
}