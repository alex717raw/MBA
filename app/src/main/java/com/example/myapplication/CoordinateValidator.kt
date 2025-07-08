package com.example.myapplication

import com.mapbox.geojson.Point

/**
 * Валидатор координат
 */
object CoordinateValidator {
    
    data class ValidationResult(
        val isValid: Boolean,
        val point: Point? = null,
        val errorMessage: String? = null
    )
    
    /**
     * Валидация строки с координатами в формате "lat, lng"
     */
    fun validateCoordinates(input: String): ValidationResult {
        val cleanInput = input.replace(" ", "")
        val parts = cleanInput.split(",")
        
        if (parts.size != 2) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Введите координаты lat, lng"
            )
        }
        
        val lat = parts[0].toDoubleOrNull()
        val lng = parts[1].toDoubleOrNull()
        
        if (lat == null || lng == null) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Некорректные координаты"
            )
        }
        
        if (lat !in -90.0..90.0 || lng !in -180.0..180.0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Некорректные координаты"
            )
        }
        
        return ValidationResult(
            isValid = true,
            point = Point.fromLngLat(lng, lat)
        )
    }
} 