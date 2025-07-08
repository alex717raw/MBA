package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation

/**
 * Главная активность приложения
 */
class MainActivity : ComponentActivity() {
    
    // Компоненты архитектуры
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var locationManager: LocationManager
    private lateinit var mapUIManager: MapUIManager
    private lateinit var cameraController: CameraController
    private lateinit var navigationManager: NavigationManager
    
    // Состояние приложения
    private var navigationState = NavigationState()
    
    // MapboxNavigation
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            override fun onAttached(mapboxNavigation: MapboxNavigation) {}
            override fun onDetached(mapboxNavigation: MapboxNavigation) {}
        },
        onInitialize = this::initNavigation
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализация обработчика разрешений
        permissionHandler = PermissionHandler(this) {
            initializeLocationAndUI()
        }
        
        // Запрос разрешений
        permissionHandler.checkAndRequestLocationPermission()
    }
    
    /**
     * Инициализация геолокации и UI
     */
    private fun initializeLocationAndUI() {
        locationManager = LocationManager(this) { centerPoint ->
            runOnUiThread {
                initializeComponents(centerPoint)
            }
        }
        locationManager.getUserLocationAndInit()
    }
    
    /**
     * Инициализация всех компонентов
     */
    private fun initializeComponents(centerPoint: Point) {
        // Обновление состояния
        navigationState = navigationState.copy(currentUserPoint = centerPoint)
        
        // Создание UI
        mapUIManager = MapUIManager(
            context = this,
            onGoClick = this::onGoClicked,
            onRefreshClick = this::onRefreshClicked,
            onCancelClick = this::onCancelClicked
        )
        
        val frameLayout = mapUIManager.createUI(centerPoint)
        setContentView(frameLayout)
        
        val mapView = mapUIManager.getMapView()
        
        // Настройка отображения геолокации
        locationManager.setupLocationDisplay(mapView) { point ->
            navigationState = navigationState.copy(currentUserPoint = point)
        }
        
        // Инициализация контроллера камеры
        cameraController = CameraController(this, mapView)
        cameraController.onFollowTimerExpired = {
            cameraController.startFollowing(navigationState.currentUserPoint)
        }
        
        // Инициализация менеджера навигации
        navigationManager = NavigationManager(this, mapView, mapboxNavigation)
    }
    
    /**
     * Инициализация MapboxNavigation
     */
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun initNavigation() {
        MapboxNavigationApp.setup(NavigationOptions.Builder(this).build())
    }
    
    /**
     * Обработка нажатия кнопки Go
     */
    private fun onGoClicked(coordinatesInput: String) {
        val validationResult = CoordinateValidator.validateCoordinates(coordinatesInput)
        
        if (!validationResult.isValid) {
            Toast.makeText(this, validationResult.errorMessage, Toast.LENGTH_SHORT).show()
            return
        }
        
        val userPoint = navigationState.currentUserPoint
        if (userPoint == null) {
            Toast.makeText(this, "Геопозиция пользователя не определена", Toast.LENGTH_SHORT).show()
            return
        }
        
        val destinationPoint = validationResult.point!!
        
        // Построение маршрута
        navigationManager.buildRoute(userPoint, destinationPoint) {
            // Обновление состояния
            navigationState = navigationState.copy(
                isRouteActive = true,
                destinationPoint = destinationPoint
            )
            
            // Уведомление CameraController о состоянии маршрута
            cameraController.setRouteActive(true)
            
            // Включение слежения
            cameraController.startFollowing(userPoint)
            
            // Очистка поля ввода
            mapUIManager.clearCoordinateInput()
        }
    }
    
    /**
     * Обработка нажатия кнопки Refresh
     */
    private fun onRefreshClicked() {
        cameraController.refreshUserLocation(navigationState.currentUserPoint)
    }
    
    /**
     * Обработка нажатия кнопки Cancel
     */
    private fun onCancelClicked() {
        if (navigationState.isRouteActive) {
            navigationManager.cancelRoute(navigationState.currentUserPoint)
            navigationState = navigationState.copy(
                isRouteActive = false,
                destinationPoint = null
            )
            
            // Уведомление CameraController об отмене маршрута
            cameraController.setRouteActive(false)
            // Добавлено: обновление карты после завершения маршрута
            onRefreshClicked()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::cameraController.isInitialized) {
            cameraController.cleanup()
        }
    }
}