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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.schiar.fiberfinder.R
import io.schiar.fiberfinder.databinding.FragmentRestaurantBinding
import io.schiar.fiberfinder.view.viewdata.LocationViewData
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData
import io.schiar.fiberfinder.viewmodel.RestaurantsViewModel
import kotlin.math.ln
import kotlin.math.roundToInt


class RestaurantFragment : Fragment(), LocationListener, OnMapReadyCallback, Observer<RestaurantViewData> {

    private lateinit var viewModel: RestaurantsViewModel
    private lateinit var binding: FragmentRestaurantBinding
    private var googleMap: GoogleMap? = null
    private var builder = LatLngBounds.Builder()
    private var LOCATION_REFRESH_TIME = 3000 // 3 seconds. The Minimum Time to get location update
    private var LOCATION_REFRESH_DISTANCE = 0 // 0 meters. The Minimum Distance to be changed to get location update
    private var currentLocation = LatLng(0.0, 0.0)
    private var radiusCircle: Circle? = null
    private var radius = 15000.0
    private var currentMarkers = mutableSetOf<LocationViewData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[RestaurantsViewModel::class.java]
        binding = FragmentRestaurantBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@RestaurantFragment
            executePendingBindings()
        }
        viewModel.restaurant.observe(viewLifecycleOwner, this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun createOrMoveUserIcon() {
        if (radiusCircle === null) {
            radiusCircle = googleMap?.addCircle(CircleOptions().center(currentLocation).radius(radius))
        } else {
            radiusCircle!!.center = currentLocation
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = LatLng(location.latitude, location.longitude)
        viewModel.fetchRestaurantLocations(location.latitude, location.longitude, radius.roundToInt())
        createOrMoveUserIcon()
        val center = CameraUpdateFactory.newLatLng(currentLocation)
        val zoom = CameraUpdateFactory.zoomTo(radius.getZoomLevel())
        googleMap?.apply {
            moveCamera(center)
            animateCamera(zoom)
        }
    }

    private fun Double.getZoomLevel(): Float {
        return if (this > 0) {
            val metrics = resources.displayMetrics
            val size = if (metrics.widthPixels < metrics.heightPixels) metrics.widthPixels
            else metrics.heightPixels
            val scale = this * size / 300000
            (16 - ln(scale) / ln(2.0)).toFloat()
        } else 16f
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
        val applicationContext = requireActivity().applicationContext
        val accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val accessCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
        val result = ContextCompat.checkSelfPermission(applicationContext, accessFineLocation)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, accessCoarseLocation)
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

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        if (!this.checkPermission()) {
            requestPermission()
        } else {
            googleMap.isMyLocationEnabled = true
            requestLocationUpdates()
        }
    }

    private fun plotMarkers(locations: List<LocationViewData>) {
        locations.forEach { location ->
            val latLng = LatLng(
                location.latitude,
                location.longitude
            )
            val markerOptions = MarkerOptions().position(latLng)
            builder.include(markerOptions.position)
            if (!currentMarkers.contains(location)) {
                currentMarkers.add(location)
                googleMap?.addMarker(markerOptions)
            }
        }
    }

    override fun onChanged(restaurant: RestaurantViewData?) {
        restaurant ?: return
        binding.restaurant = restaurant
        (activity as AppCompatActivity).supportActionBar?.title = restaurant.name
        if (restaurant.locations.isEmpty()) return
        plotMarkers(restaurant.locations)
    }
}