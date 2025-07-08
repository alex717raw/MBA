package com.example.myapplication

import android.content.Context
import android.widget.Toast
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.PuckBearing

/**
 * Менеджер геолокации
 */
class LocationManager(
    private val context: Context,
    private val onLocationReceived: (Point) -> Unit
) {
    
    private var currentUserPoint: Point? = null
    
    /**
     * Получение текущей позиции пользователя
     */
    fun getUserLocationAndInit() {
        // Включаем временный MapView для получения позиции
        val tempMapView = MapView(context)
        tempMapView.location.enabled = true
        tempMapView.location.addOnIndicatorPositionChangedListener { point ->
            tempMapView.location.enabled = false
            tempMapView.onStop()
            tempMapView.onDestroy()
            currentUserPoint = point
            onLocationReceived(point)
        }
        
        // Таймаут на ошибку (например, если позиция не получена за 5 сек)
        tempMapView.postDelayed({
            if (currentUserPoint == null) {
                val moscow = Point.fromLngLat(37.618423, 55.751244)
                Toast.makeText(
                    context,
                    "Ошибка получения геопозиции, центр на Москве",
                    Toast.LENGTH_LONG
                ).show()
                onLocationReceived(moscow)
            }
        }, 5000)
    }
    
    /**
     * Настройка отображения позиции пользователя на карте
     */
    fun setupLocationDisplay(mapView: MapView, onPositionChanged: (Point) -> Unit) {
        mapView.location.updateSettings {
            enabled = true
            locationPuck = createDefault2DPuck(withBearing = true)
            pulsingEnabled = true
            puckBearing = PuckBearing.HEADING
            puckBearingEnabled = true
        }
        
        mapView.location.addOnIndicatorPositionChangedListener { point ->
            currentUserPoint = point
            onPositionChanged(point)
        }
    }
    
    /**
     * Получение текущей позиции пользователя
     */
    fun getCurrentUserPoint(): Point? = currentUserPoint
} 