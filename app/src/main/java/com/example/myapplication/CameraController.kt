package com.example.myapplication

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.viewport.viewport

/**
 * Контроллер камеры и слежения
 */
class CameraController(
    private val context: Context,
    private val mapView: MapView
) {
    
    private var isFollowing = false
    private var followTimerHandler: Handler? = null
    private var followTimerRunnable: Runnable? = null
    private var isRouteActive = false
    
    init {
        setupGestureListener()
    }
    
    /**
     * Настройка слушателя жестов для сброса слежения
     */
    private fun setupGestureListener() {
        mapView.gestures.addOnMoveListener(object : OnMoveListener {
            override fun onMoveBegin(detector: com.mapbox.android.gestures.MoveGestureDetector) {
                stopFollowing()
                startFollowTimer()
            }
            override fun onMove(detector: com.mapbox.android.gestures.MoveGestureDetector): Boolean = false
            override fun onMoveEnd(detector: com.mapbox.android.gestures.MoveGestureDetector) {}
        })
    }
    
    /**
     * Установка камеры на указанную точку
     */
    fun setCameraToPoint(point: Point, zoom: Double = 15.0) {
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(point)
                .zoom(zoom)
               // .bearing(0.0) // Карта строго на север
                .pitch(0.0)   // Без наклона
                .build()
        )
    }
    
    /**
     * Включение слежения за пользователем
     */
    fun startFollowing(userPoint: Point?) {
        if (userPoint != null) {
            val followState = mapView.viewport.makeFollowPuckViewportState()
            mapView.viewport.transitionTo(followState)
            // Программно выставляем зум с правильной ориентацией
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .zoom(10.0)
                    //.bearing(0.0) // Карта строго на север
                    .pitch(0.0)   // Без наклона
                    .build()
            )
            isFollowing = true
        }
    }
    
    /**
     * Остановка слежения
     */
    fun stopFollowing() {
        isFollowing = false
    }
    
    /**
     * Запуск таймера для возврата к слежению (только если есть активный маршрут)
     */
    private fun startFollowTimer() {
        if (followTimerRunnable != null) {
            followTimerHandler?.removeCallbacks(followTimerRunnable!!)
        }
        if (followTimerHandler == null) {
            followTimerHandler = Handler(Looper.getMainLooper())
        }
        followTimerRunnable = Runnable {
            if (!isFollowing && isRouteActive) {
                // Автоцентрирование только при активном маршруте
                onFollowTimerExpired?.invoke()
            }
        }
        followTimerHandler?.postDelayed(followTimerRunnable!!, 5000) // 5 секунд
    }
    
    /**
     * Обновление позиции пользователя с Toast
     */
    fun refreshUserLocation(userPoint: Point?) {
        try {
            if (userPoint != null) {
                setCameraToPoint(userPoint)
                Toast.makeText(context, "Карта центрирована на пользователе", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Геопозиция недоступна", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка обновления позиции: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Очистка ресурсов
     */
    fun cleanup() {
        if (followTimerRunnable != null) {
            followTimerHandler?.removeCallbacks(followTimerRunnable!!)
        }
    }
    
    /**
     * Callback для срабатывания таймера слежения
     */
    var onFollowTimerExpired: (() -> Unit)? = null
    
    /**
     * Проверка активности слежения
     */
    fun isFollowing(): Boolean = isFollowing
    
    /**
     * Установка состояния активности маршрута
     */
    fun setRouteActive(active: Boolean) {
        isRouteActive = active
    }
} 