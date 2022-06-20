package io.schiar.fiberfinder.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.schiar.fiberfinder.databinding.FragmentRestaurantBinding
import io.schiar.fiberfinder.viewmodel.RestaurantsViewModel

class RestaurantFragment : Fragment() {

    private lateinit var viewModel: RestaurantsViewModel

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
        }
        return binding.root
    }
}