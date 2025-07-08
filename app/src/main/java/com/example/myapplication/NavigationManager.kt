package com.example.myapplication

import android.content.Context
import android.widget.Toast
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
//import com.mapbox.maps.plugin.viewport.data.ViewportFollowingFrameOptions
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import android.os.Handler
import android.os.Looper

/**
 * Менеджер навигации и маршрутов
 */
class NavigationManager(
    private val context: Context,
    private val mapView: MapView,
    private val mapboxNavigation: MapboxNavigation
) {
    
    private val viewportDataSource: MapboxNavigationViewportDataSource
    private val navigationCamera: NavigationCamera
    private val routeLineApi: MapboxRouteLineApi
    private val routeLineView: MapboxRouteLineView
    
    private var isRouteActive = false
    private var northResetHandler: Handler? = null
    private var northResetRunnable: Runnable? = null
    
    init {
        // Инициализация компонентов навигации
        viewportDataSource = MapboxNavigationViewportDataSource(mapView.mapboxMap)
        val pixelDensity = context.resources.displayMetrics.density
        viewportDataSource.followingPadding = EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
        // Удалено: управление bearing
        navigationCamera = NavigationCamera(mapView.mapboxMap, mapView.camera, viewportDataSource)
        routeLineApi = MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
        routeLineView = MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(context).build())
    }
    
    /**
     * Построение маршрута между двумя точками
     */
    fun buildRoute(origin: Point, destination: Point, onRouteBuilt: () -> Unit) {
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(origin, destination))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    Toast.makeText(context, "Построение маршрута отменено", Toast.LENGTH_SHORT).show()
                }
                
                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    Toast.makeText(context, "Ошибка построения маршрута", Toast.LENGTH_SHORT).show()
                }
                
                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    if (routes.isNotEmpty()) {
                        mapboxNavigation.setNavigationRoutes(routes)
                        isRouteActive = true
                        
                        // Обновление viewport
                        viewportDataSource.onRouteChanged(routes.first())
                        viewportDataSource.evaluate()
                        navigationCamera.requestNavigationCameraToFollowing()
                        
                        // Отрисовка линии маршрута
                        routeLineApi.setNavigationRoutes(routes) { value ->
                            mapView.mapboxMap.getStyle { style ->
                                routeLineView.renderRouteDrawData(style, value)
                            }
                        }
                        
                        Toast.makeText(context, "Маршрут построен", Toast.LENGTH_SHORT).show()
                        onRouteBuilt()
                    } else {
                        Toast.makeText(context, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
    
    /**
     * Отмена текущего маршрута
     */
    fun cancelRoute(userPoint: Point?) {
        if (isRouteActive) {
            mapboxNavigation.setNavigationRoutes(emptyList())
            isRouteActive = false
            // Сброс линии маршрута
            routeLineApi.clearRouteLine { value ->
                mapView.mapboxMap.getStyle { style ->
                    routeLineView.renderClearRouteLineValue(style, value)
                }
            }
            // Центрирование и сброс ориентации (1 секунда автосброса)
            val center = userPoint ?: Point.fromLngLat(37.618423, 55.751244)
            startNorthReset(center)
            Toast.makeText(context, "Маршрут удалён", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startNorthReset(center: Point) {
        northResetHandler?.removeCallbacks(northResetRunnable ?: Runnable { })
        if (northResetHandler == null) northResetHandler = Handler(Looper.getMainLooper())
        var elapsed = 0L
        val interval = 100L
        val duration = 1000L
        northResetRunnable = object : Runnable {
            override fun run() {
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(center)
                        .zoom(15.0)
                        .bearing(0.0)
                        .pitch(0.0)
                        .build()
                )
                elapsed += interval
                if (elapsed < duration) {
                    northResetHandler?.postDelayed(this, interval)
                }
            }
        }
        northResetHandler?.post(northResetRunnable!!)
    }
    
    /**
     * Проверка активности маршрута
     */
    fun isRouteActive(): Boolean = isRouteActive
    
    /**
     * Получение viewport data source для внешнего использования
     */
    fun getViewportDataSource(): MapboxNavigationViewportDataSource = viewportDataSource
    
    /**
     * Получение navigation camera для внешнего использования
     */
    fun getNavigationCamera(): NavigationCamera = navigationCamera
} 