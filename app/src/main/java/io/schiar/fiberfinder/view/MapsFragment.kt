package io.schiar.fiberfinder.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.schiar.fiberfinder.R
import io.schiar.fiberfinder.view.viewdata.LocationViewData
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData
import io.schiar.fiberfinder.viewmodel.RestaurantsViewModel
import kotlin.math.roundToInt

class MapsFragment :
    Fragment(),
    LocationListener,
    OnMapReadyCallback,
    GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMarkerClickListener,
    Observer<List<RestaurantViewData>> {

    private lateinit var viewModel: RestaurantsViewModel
    private var map: GoogleMap? = null
    private var LOCATION_REFRESH_TIME = 3000 // 3 seconds. The Minimum Time to get location update
    private var LOCATION_REFRESH_DISTANCE = 0 // 0 meters. The Minimum Distance to be changed to get location update
    private var radius = 1000.0
    private lateinit var markerColors: List<Float>
    private var currentMarkers = mutableMapOf<LocationViewData, Pair<Marker?, Boolean>>()
    private var cameraMoved = false
    private var currentLocation = LatLng(0.0, 0.0)
    private var circle: Circle? = null
    private val activityRegister = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        onPermissionsGranted(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityRegister.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        markerColors = listOf(
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_YELLOW
        )
        viewModel = ViewModelProvider(requireActivity())[RestaurantsViewModel::class.java]
        viewModel.restaurants.observe(viewLifecycleOwner, this)
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun requestLocationUpdates() {
        val mLocationManager = context?.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager
        mLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_REFRESH_TIME.toLong(),
            LOCATION_REFRESH_DISTANCE.toFloat(),
            this
        )
    }

    private fun checkPermission(): Boolean {
        //val applicationContext = requireActivity().applicationContext
        val accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val accessCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
        val result = requireActivity().checkSelfPermission(accessFineLocation)
        val result1 = requireActivity().checkSelfPermission(accessCoarseLocation)
        return result == PackageManager.PERMISSION_GRANTED
                && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(requireActivity(), permissions,1)
    }

    private fun onPermissionsGranted(permission: Boolean) {
        if (permission) {
            requestLocationUpdates()
            map?.isMyLocationEnabled = true
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        if (!this.checkPermission()) {
            requestPermission()
        } else {
            map.isMyLocationEnabled = true
            map.setOnCameraMoveStartedListener(this)
            map.setOnMyLocationButtonClickListener(this)
            map.setOnMarkerClickListener(this)
        }
    }

    private fun moveToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val center = CameraUpdateFactory.newLatLng(latLng)
        val zoom = CameraUpdateFactory.zoomTo(radius.getZoomLevel(context, resources))
        if (!cameraMoved) {
            map?.apply {
                moveCamera(center)
                animateCamera(zoom)
                circle = circle ?: addCircle(CircleOptions().center(latLng).radius(radius))
                circle?.center = latLng
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        moveToLocation(location)
        viewModel.fetchAllRestaurantLocations(location.latitude, location.longitude, radius.roundToInt())
    }

    private fun plotMarkers(name: String, locations: List<LocationViewData>, color: Float) {
        locations.forEach { location ->
            val latLng = LatLng(
                location.latitude,
                location.longitude
            )
            val markerOptions = MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .title(name)
            if (!currentMarkers.contains(location)) {
                currentMarkers[location] = Pair(map?.addMarker(markerOptions), true)
            } else {
                val value = currentMarkers[location]
                currentMarkers[location] = Pair(value?.first, true)
            }
        }
    }

    private fun invalidateAllMarkers() {
        for (markerKey in currentMarkers.keys) {
            val value = currentMarkers[markerKey]
            currentMarkers[markerKey] = Pair(value?.first, false)
        }
    }

    private fun removeOldMarkers() {
        currentMarkers.filter { !it.value.second }.forEach { (key, value) ->
            val marker = value.first
            marker?.remove()
            currentMarkers.remove(key)
        }
    }

    override fun onChanged(restaurants: List<RestaurantViewData>?) {
        map ?: return
        var i = 0
        invalidateAllMarkers()
        restaurants?.filter {it.locations.isNotEmpty()}?.forEach {
            plotMarkers(it.name, it.locations, markerColors[i])
            i = (i + 1) % markerColors.size
        }
        removeOldMarkers()
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == REASON_GESTURE) {
            cameraMoved = true
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        cameraMoved = false
        return false
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        cameraMoved = true
        return false
    }
}