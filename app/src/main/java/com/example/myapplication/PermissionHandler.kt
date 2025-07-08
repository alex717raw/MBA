package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

/**
 * Обработчик разрешений
 */
class PermissionHandler(
    private val activity: ComponentActivity,
    private val onPermissionGranted: () -> Unit
) {
    
    private val locationPermissionRequest =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    onPermissionGranted()
                }
                else -> {
                    Toast.makeText(
                        activity,
                        "Location permissions denied. Please enable permissions in settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    
    /**
     * Проверка и запрос разрешений на геолокацию
     */
    fun checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted()
        } else {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
} 