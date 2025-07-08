package com.example.myapplication

import com.mapbox.geojson.Point

/**
 * Состояние навигации приложения
 */
data class NavigationState(
    val currentUserPoint: Point? = null,
    val isRouteActive: Boolean = false,
    val isFollowing: Boolean = false,
    val destinationPoint: Point? = null
) 