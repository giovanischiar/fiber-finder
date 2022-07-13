package io.schiar.fiberfinder.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.progressindicator.LinearProgressIndicator
import io.schiar.fiberfinder.R
import io.schiar.fiberfinder.view.viewdata.LocationViewData
import io.schiar.fiberfinder.viewmodel.RestaurantsViewModel
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class MapsFragment :
    Fragment(),
    LocationListener,
    OnMapReadyCallback,
    GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMarkerClickListener,
    OnRadiusChangeButtonClickedListener {

    private lateinit var viewModel: RestaurantsViewModel
    private var map: GoogleMap? = null
    private var LOCATION_REFRESH_TIME = 3000 // 3 seconds. The Minimum Time to get location update
    private var LOCATION_REFRESH_DISTANCE = 20 // 0 meters. The Minimum Distance to be changed to get location update
    private var radius = 6000.0
    private var cameraMoved = false
    private var zoomChanged = false
    private var currentZoom = 0f
    private var circle: Circle? = null
    private var currentLocation: Location? = null
    private val activityRegister = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        onPermissionsGranted(it)
    }

    private val markers = mutableMapOf<LocationViewData, Marker>()

    private fun setObservers() {
        viewModel.restaurantAmount.observe(viewLifecycleOwner) {
            val progressIndicatorID = R.id.linear_progress_indicator
            val progressIndicator = (view ?: return@observe)
                .findViewById<LinearProgressIndicator>(progressIndicatorID)
            progressIndicator.max = it
        }

        viewModel.markersToAdd.observe(viewLifecycleOwner) { markersToAdd ->
            var adding = 0
            println("# adding ${markersToAdd.size} markers... markers now: ${markers.keys}")
            for (entry in markersToAdd.filter { !markers.containsKey(it.key) }) {
                val (locationViewData, markerViewData) = entry
                markers[locationViewData] = generateAndAddNewMarker(
                    markerViewData.name,
                    locationViewData.toLatLng(),
                    markerViewData.color
                ) ?: continue
                adding++
            }

            println("# $adding markers added! markers now: ${markers.keys}")
        }

        viewModel.markersToRem.observe(viewLifecycleOwner) { markersToRem ->
            var removing = 0
            println("# removing ${markersToRem.size} markers... markers now: ${markers.keys}")
            for (locationViewData in markersToRem.keys) {
                (markers[locationViewData] ?: continue).remove()
                markers.remove(locationViewData)
                removing++
            }
            println("# $removing markers removed! markers now: ${markers.keys}")
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            progress ?: return@observe
            val progressIndicatorID = R.id.linear_progress_indicator
            val progressIndicator = (view ?: return@observe)
                .findViewById<LinearProgressIndicator>(progressIndicatorID)
            progressIndicator ?: return@observe
            progressIndicator.apply {
                if (progress != progressIndicator.max) {
                    show()
                    this.progress = progress
                } else {
                    hide()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityRegister.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        viewModel = ViewModelProvider(requireActivity())[RestaurantsViewModel::class.java]
        setObservers()
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        view.findViewById<Button>(R.id.search_here_btn).setOnClickListener {
            it.visibility = View.GONE
            val centerLatLng = map?.cameraPosition?.target
            centerLatLng ?: return@setOnClickListener
            currentLocation?.latitude =  centerLatLng.latitude
            currentLocation?.longitude =  centerLatLng.longitude

            val bounds = map?.projection?.visibleRegion?.latLngBounds
            val llNeLat = bounds?.northeast?.latitude
            val llSwLat = bounds?.southwest?.latitude
            val llNeLng = bounds?.northeast?.longitude
            val llSwLng = bounds?.southwest?.longitude
            val results = FloatArray(5)
            Location.distanceBetween(
                llNeLat ?: return@setOnClickListener,
                llNeLng ?: return@setOnClickListener,
                llSwLat ?: return@setOnClickListener,
                llSwLng ?: return@setOnClickListener,
                results
            )
            radius = (results[0].toDouble())/2
            viewModel.fetchAllRestaurantLocations(
                centerLatLng.latitude,
                centerLatLng.longitude,
                radius.roundToInt()
            )
            moveToLocation(currentLocation ?: return@setOnClickListener)
        }
    }

    override fun onRadiusButtonClicked() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Set radius (in meters)")
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(radius.toString())
        builder.setView(input)
        builder.setPositiveButton("OK") { _, _ ->
            radius = input.text.toString().toDouble()
            onLocationChanged(currentLocation ?: return@setPositiveButton)
        }
        builder.show()
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
        map?.apply {
            moveCamera(center)
            circle = circle ?: addCircle(CircleOptions().center(latLng).radius(radius))
            circle?.center = latLng
            circle?.radius = radius
        }
    }

    private fun zoomToRadius() {
        currentZoom = (radius/2).getZoomLevel(context)
        val zoom = CameraUpdateFactory.zoomTo(currentZoom)
        map?.apply {
            animateCamera(zoom)
        }
        zoomChanged = false
    }

    override fun onLocationChanged(location: Location) {
        if (cameraMoved) return
        currentLocation = location
        moveToLocation(location)
        zoomToRadius()
        val latitude = location.latitude
        val longitude = location.longitude
        viewModel.fetchAllRestaurantLocations(latitude, longitude, radius.roundToInt())
    }

    private fun generateAndAddNewMarker(name: String, latLng: LatLng, color: Float): Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(color)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(bitmapDescriptor)
            .title(name)
        return (map ?: return null).addMarker(markerOptions)
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == REASON_GESTURE) {
            cameraMoved = true
            view?.findViewById<Button>(R.id.search_here_btn)?.visibility = View.VISIBLE
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        cameraMoved = false
        zoomToRadius()
        view?.findViewById<Button>(R.id.search_here_btn)?.visibility = View.GONE
        return false
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        cameraMoved = true
        return false
    }
}