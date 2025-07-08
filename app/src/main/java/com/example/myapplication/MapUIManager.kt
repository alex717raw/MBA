package com.example.myapplication

import android.content.Context
import android.widget.FrameLayout
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.compass.compass

/**
 * Менеджер UI карты
 */
class MapUIManager(
    private val context: Context,
    private val onGoClick: (String) -> Unit,
    private val onRefreshClick: () -> Unit,
    private val onCancelClick: () -> Unit
) {
    
    private lateinit var mapView: MapView
    private lateinit var frameLayout: FrameLayout
    private lateinit var coordinateInputView: CoordinateInputView
    private lateinit var navigationButtons: NavigationButtons
    
    /**
     * Создание и настройка UI
     */
    fun createUI(centerPoint: Point): FrameLayout {
        // Создание MapView
        mapView = MapView(context)
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(centerPoint)
                .zoom(15.0)
                .bearing(0.0) // Карта строго на север
                .pitch(0.0)   // Без наклона
                .build()
        )
        
        // Настройка компаса для правильного поворота по центру
        mapView.compass.updateSettings {
            enabled = true
            fadeWhenFacingNorth = true
        }
        
        // Создание основного контейнера
        frameLayout = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        // Добавление MapView в контейнер
        frameLayout.addView(mapView)
        
        // Создание и добавление компонентов UI
        setupInputView()
        setupNavigationButtons()
        
        return frameLayout
    }
    
    /**
     * Настройка поля ввода координат
     */
    private fun setupInputView() {
        coordinateInputView = CoordinateInputView(context)
        coordinateInputView.addToContainer(frameLayout)
    }
    
    /**
     * Настройка кнопок навигации
     */
    private fun setupNavigationButtons() {
        navigationButtons = NavigationButtons(context)
        navigationButtons.addToContainer(frameLayout)
        
        // Установка обработчиков
        navigationButtons.setOnClickListeners(
            onGoClick = { onGoClick(coordinateInputView.getText()) },
            onRefreshClick = onRefreshClick,
            onCancelClick = onCancelClick
        )
    }
    
    /**
     * Получение MapView
     */
    fun getMapView(): MapView = mapView
    
    /**
     * Очистка поля ввода координат
     */
    fun clearCoordinateInput() {
        coordinateInputView.clearText()
    }
    
    /**
     * Получение компонентов UI
     */
    fun getCoordinateInputView(): CoordinateInputView = coordinateInputView
    fun getNavigationButtons(): NavigationButtons = navigationButtons
} 