package io.schiar.fiberfinder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import io.schiar.fiberfinder.R
import io.schiar.fiberfinder.databinding.FragmentRestaurantBinding
import io.schiar.fiberfinder.viewmodel.RestaurantsViewModel
import kotlin.math.roundToInt


class RestaurantFragment : Fragment() {

    private lateinit var viewModel: RestaurantsViewModel
    private var googleMap: GoogleMap? = null
    private var builder = LatLngBounds.Builder()

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        viewModel.fetchRestaurantLocations()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[RestaurantsViewModel::class.java]
        val binding = FragmentRestaurantBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@RestaurantFragment
            executePendingBindings()
        }
        viewModel.restaurant.observe(viewLifecycleOwner) {
            binding.restaurant = it
            (activity as AppCompatActivity).supportActionBar?.title = it.name
            if (it.locations.isEmpty()) return@observe
            it.locations.forEach { location ->
                val latLng = LatLng(
                    location.latitude.toDouble(),
                    location.longitude.toDouble()
                )
                val marker = MarkerOptions().position(latLng)
                builder.include(marker.position)
                googleMap?.addMarker(marker)
            }
            googleMap ?: return@observe
            val bounds = builder.build()
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, resources.getDimension(R.dimen.map_zoom_padding).roundToInt());
            googleMap?.animateCamera(cu)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}