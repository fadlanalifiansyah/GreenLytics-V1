package com.example.greenlytics.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class LocationDetail(val cityName: String?, val lat: Double?, val lon: Double?)

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationDetail {
        return try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                val city = if (!addresses.isNullOrEmpty()) {
                    addresses[0].subAdminArea ?: addresses[0].locality
                } else null

                LocationDetail(city, location.latitude, location.longitude)
            } else {
                LocationDetail(null, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LocationDetail(null, null, null)
        }
    }
}