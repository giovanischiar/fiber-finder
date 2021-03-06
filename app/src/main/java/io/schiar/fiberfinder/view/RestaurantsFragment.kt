package io.schiar.fiberfinder.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.schiar.fiberfinder.R
import io.schiar.fiberfinder.databinding.FragmentRestaurantsBinding
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData
import io.schiar.fiberfinder.viewmodel.RestaurantsViewModel

class RestaurantsFragment :
    Fragment(),
    SelectedRestaurantListener,
    RestaurantCheckedChangedListener {

    private lateinit var viewModel: RestaurantsViewModel
    private val currentRestaurants = mutableSetOf<RestaurantViewData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[RestaurantsViewModel::class.java]
        val binding = FragmentRestaurantsBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@RestaurantsFragment
            viewModel = this@RestaurantsFragment.viewModel
            executePendingBindings()
        }
        viewModel.apply {
            fetch()
            currentRestaurants.observe(viewLifecycleOwner) {
                binding.adapter = RestaurantListAdapter(
                    it,
                    this@RestaurantsFragment,
                    this@RestaurantsFragment
                )
            }
//            restaurants.observe(viewLifecycleOwner) {
//                binding.adapter = RestaurantListAdapter(
//                    it.filter { restaurantViewData -> restaurantViewData.locations.isNotEmpty() },
//                    this@RestaurantsFragment,
//                    this@RestaurantsFragment
//                )
//            }
//
//            partialRestaurantLocation.observe(viewLifecycleOwner) {
//                currentRestaurants.add(it)
//                val restaurantListAdapter = RestaurantListAdapter(
//                    currentRestaurants.filter { restaurant -> restaurant.locations.isNotEmpty() }.toList(),
//                    this@RestaurantsFragment,
//                    this@RestaurantsFragment
//                )
//                binding.adapter = restaurantListAdapter
//            }
        }
        return binding.root

    }

    override fun onSelectRestaurant(index: Int) {
        viewModel.restaurantAt(index)
        findNavController().navigate(R.id.action_RestaurantsFragment_to_RestaurantFragment)
    }

    override fun onRestaurantCheckedChanged(index: Int) {
        viewModel.changeIsShownRestaurantAt(index)
    }
}