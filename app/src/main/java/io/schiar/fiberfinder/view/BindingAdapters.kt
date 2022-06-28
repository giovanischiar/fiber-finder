package io.schiar.fiberfinder.view

import android.graphics.Color
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import io.schiar.fiberfinder.viewmodel.MarkerColors

object BindingAdapters {
    @BindingAdapter(value=["marker"])
    @JvmStatic
    fun setMarker(layout: FrameLayout, markerColor: MarkerColors) {
        layout.setBackgroundColor(
            Color.HSVToColor(floatArrayOf(markerColor.floatValue, 1.0f, 1.0f))
        )
    }
}