package io.schiar.fiberfinder.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.schiar.fiberfinder.R
import io.schiar.fiberfinder.databinding.RestaurantAdapterBinding
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData

class RestaurantListAdapter(
    private val restaurants: List<RestaurantViewData>,
    private val selectedRestaurantListener: SelectedRestaurantListener,
    private val restaurantCheckedChangedListener: RestaurantCheckedChangedListener
) :
    RecyclerView.Adapter<RestaurantListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RestaurantAdapterBinding>(
            inflater,
            R.layout.restaurant_adapter,
            parent,
            false
        )
        return ViewHolder(binding, selectedRestaurantListener, restaurantCheckedChangedListener)
    }

    override fun getItemCount(): Int {
        return restaurants.size
    }

    override fun onBindViewHolder(holder: ViewHolder, index: Int) {
        holder.bind(restaurants[index], index)
    }

    class ViewHolder(
        private val binding: RestaurantAdapterBinding,
        private val selectedRestaurantListener: SelectedRestaurantListener,
        private val restaurantCheckedChangedListener: RestaurantCheckedChangedListener
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(restaurant: RestaurantViewData, index: Int) {
            binding.apply {
                this.restaurant = restaurant
                this.index = index
                this.selectedRestaurantListener = this@ViewHolder.selectedRestaurantListener
                this.restaurantCheckedChangedListener = this@ViewHolder.restaurantCheckedChangedListener
                executePendingBindings()
            }
        }
    }
}