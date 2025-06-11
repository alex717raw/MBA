package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity(), PermissionsListener {

    lateinit var permissionsManager: PermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        var context = baseContext
        var d = applicationContext
        if (PermissionsManager.areLocationPermissionsGranted(context = d)) {
            // Permission sensitive logic called here
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
        super.onCreate(savedInstanceState)

        setContent {
            MyAppContent()
        }
    }

    @Composable
    fun MyAppContent() {
        var address by remember { mutableStateOf("") }
        var routeActive by remember { mutableStateOf(false) }
        var centerRequest by remember { mutableStateOf(0) }   // триггер для ресентра камеры
        var destination by remember { mutableStateOf<Point?>(null) }
        val context = LocalContext.current

        Box(modifier = Modifier.fillMaxSize()) {

            // =====  КАРТА  =====
            MapContent(
                modifier = Modifier.matchParentSize(),
                routeActive = routeActive,
                centerRequest = centerRequest,
                destination = destination
            )

            // =====  ПОЛЕ ВВОДА + ОК  =====
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = { Text("55.978074, 37.101952") },
                    modifier = Modifier
                        .weight(1f)
                )
                IconButton(
                    onClick = {
                        val parts = address.split(",").map { it.trim() }
                        if (parts.size == 2) {
                            try {
                                val lat = parts[0].toDouble()
                                val lon = parts[1].toDouble()
                                destination = Point.fromLngLat(lon, lat)
                                // здесь можно сохранить координаты в состояние / ViewModel
                                routeActive = true
                            } catch (e: Exception) {
                                Toast.makeText(context, "Неверный формат координат", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Введите координаты через запятую", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "OK")
                }
            }

            // =====  FAB «Местоположение»  =====
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 88.dp),
                onClick = {
                    centerRequest += 1   // заставляем MapContent снова перей‑ти в FollowPuck
                }
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Моё местоположение")
            }

            // =====  FAB «Завершить маршрут»  =====
            if (routeActive) {
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 160.dp),   // выше кнопки локации
                    onClick = {
                        routeActive = false
                        // здесь можно очистить маршрут из Mapbox
                    }
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Завершить маршрут")
                }
            }
        }
    }


    @Composable
    fun MapContent(
        modifier: Modifier = Modifier,
        routeActive: Boolean,
        centerRequest: Int,
        destination: Point?
    ) {
        val mapViewportState = rememberMapViewportState()
        val context = LocalContext.current
        var polylineManager by remember { mutableStateOf<PolylineAnnotationManager?>(null) }
        var routePoints by remember { mutableStateOf<List<Point>?>(null) }
        var routeRequested by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            mapViewportState.transitionToFollowPuckState(
                FollowPuckViewportStateOptions.Builder()
                    .pitch(0.0)
                    .build()
            )
        }
        LaunchedEffect(centerRequest) {
            mapViewportState.transitionToFollowPuckState(
                FollowPuckViewportStateOptions.Builder()
                    .pitch(0.0)
                    .build()
            )
        }

        MapboxMap(
            modifier = modifier,
            mapViewportState = mapViewportState,
        ) {
            MapEffect(routeActive, destination) { mapView ->
                // --- Location puck settings ---
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    pulsingEnabled = true
                    pulsingColor = Color.BLUE
                    puckBearing = PuckBearing.HEADING
                    puckBearingEnabled = true
                }

                // --- Init polyline manager once ---
                if (polylineManager == null) {
                    polylineManager = mapView.annotations.createPolylineAnnotationManager()
                }
                val mgr = polylineManager ?: return@MapEffect

                // Clear previous drawings
                mgr.deleteAll()

                // Always reset decoded points when destination changes
                if (!routeActive) {
                    routePoints = null
                    routeRequested = false
                }

                if (routeActive && destination != null) {
                    // If we already have decoded points, just draw them
                    if (routePoints != null) {
                        val polyline = PolylineAnnotationOptions()
                            .withPoints(routePoints!!)
                            .withLineColor("#3B82F6")
                            .withLineWidth(6.0)
                        mgr.create(polyline)
                    } else if (!routeRequested) {
                        val latest = mapView.location.latestLocation
                        if (latest != null) {
                            routeRequested = true
                            val origin = Point.fromLngLat(latest.longitude, latest.latitude)
                            // Build Directions request
                            val client = MapboxDirections.builder()
                                .origin(origin)
                                .destination(destination)
                                .overview(DirectionsCriteria.OVERVIEW_FULL)
                                .profile(DirectionsCriteria.PROFILE_DRIVING)
                                .accessToken(mapView.context.getString(R.string.mapbox_access_token))
                                .build()

                            client.enqueueCall(object : Callback<DirectionsResponse> {
                                override fun onResponse(
                                    call: Call<DirectionsResponse>,
                                    response: Response<DirectionsResponse>
                                ) {
                                    routeRequested = false
                                    val route = response.body()?.routes()?.firstOrNull()
                                    val geometry = route?.geometry()
                                    if (geometry != null) {
                                        val coords = LineString.fromPolyline(geometry, 6).coordinates()
                                        routePoints = coords
                                    }
                                }

                                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                                    routeRequested = false
                                    // Could log or show a Toast with error
                                }
                            })
                        }
                    }
                } else {
                    // Reset when route is inactive
                    routePoints = null
                    routeRequested = false
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        // Реализуйте при необходимости
    }

    override fun onPermissionResult(granted: Boolean) {
        // Реализуйте при необходимости
    }
}