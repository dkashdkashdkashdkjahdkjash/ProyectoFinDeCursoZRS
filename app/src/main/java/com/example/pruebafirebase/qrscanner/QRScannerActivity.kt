package com.example.pruebafirebase.qrscanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

// La activity del escaner
class QRScannerActivity : ComponentActivity() {
    // Al crearse
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startScan()
    }

    // Al empezar a escanear
    private fun startScan() {
        launcher.launch(
            ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Escanea el QR")
                setBeepEnabled(true)
                setOrientationLocked(true)
                setCaptureActivity(CustomCaptureActivity::class.java)
            }
        )
    }

    // Pasa lo que escanea
    private val launcher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val data = Intent().apply {
                putExtra("qr_id", result.contents)
            }
            setResult(RESULT_OK, data)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }


}
