package io.schiar.fiberfinder.view

import android.content.Context
import android.content.res.Resources
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import io.schiar.fiberfinder.viewmodel.MarkerColors
import kotlin.math.ln

fun Double.getZoomLevel(context: Context?): Float {
    return if (this > 0) {
        context ?: return 16f
        val metrics = context.resources.displayMetrics
        val size = if (metrics.widthPixels < metrics.heightPixels) metrics.widthPixels
        else metrics.heightPixels
        val scale = this * size / 300000
        (16 - ln(scale) / ln(2.0)).toFloat()
    } else 16f
}