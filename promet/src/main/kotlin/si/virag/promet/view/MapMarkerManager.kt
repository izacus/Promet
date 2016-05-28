package si.virag.promet.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import rx.Observable
import si.virag.promet.R
import si.virag.promet.model.data.EventGroup
import si.virag.promet.model.data.TrafficCounter
import si.virag.promet.model.data.TrafficEvent
import si.virag.promet.model.data.TrafficStatus

class MapMarkerManager(val context : Context) {

    val RED_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    val ORANGE_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
    val GREEN_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
    val YELLOW_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
    val AZURE_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
    val CONE_MARKER = BitmapDescriptorFactory.fromResource(R.drawable.map_cone)

    val TRAFFIC_DENSITY_COLORS : Array<Pair<Int, Int>> =
            arrayOf(Pair(Color.TRANSPARENT, Color.TRANSPARENT),                         // NO DATA
                    Pair(Color.argb(140, 102, 255, 0), Color.argb(64, 102, 255, 0)),    // NORMAL TRAFFIC
                    Pair(Color.argb(100, 242, 255, 0), Color.argb(160, 242, 255, 0)),   // INCREASED TRAFFIC
                    Pair(Color.argb(180, 255, 208, 0), Color.argb(256, 255, 184, 0)),   // DENSER TRAFFIC
                    Pair(Color.argb(180, 255, 18, 0), Color.argb(256, 255, 18, 0)),     // DENSE TRAFFIC
                    Pair(Color.argb(200, 255, 0, 0), Color.argb(256, 255, 0, 0))
            )

    val TRAFFIC_DENSITY_BITMAPS : Array<BitmapDescriptor> by lazy {
        // Prepare circles for traffic density
        val p = Paint()
        val borderPaint = Paint()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 3.0f
        borderPaint.isAntiAlias = true
        val circleRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7.0f, context.resources.displayMetrics)

        val bitmaps = Array<BitmapDescriptor>(TRAFFIC_DENSITY_COLORS.size, { i ->
                val bmp = Bitmap.createBitmap(circleRadius.toInt() * 2, circleRadius.toInt() * 2, Bitmap.Config.ARGB_8888)
                p.color = TRAFFIC_DENSITY_COLORS[i].first
                borderPaint.color = TRAFFIC_DENSITY_COLORS[i].second
                val canvas = Canvas(bmp)
                canvas.drawCircle(circleRadius, circleRadius, circleRadius, p)
                canvas.drawCircle(circleRadius, circleRadius, circleRadius, borderPaint)
                BitmapDescriptorFactory.fromBitmap(bmp)
        })

        bitmaps
    }

    fun getEventMarkers(events : Observable<TrafficEvent>) : Observable<Pair<Int, MarkerOptions>> {
        return events.map {
            val opts = MarkerOptions()
            opts.position(LatLng(it.lat.toDouble(), it.lng.toDouble()))
            val icon : BitmapDescriptor

            if (it.isHighPriority) {
                icon = RED_MARKER
            } else if (it.isRoadworks) {
                icon = CONE_MARKER
                opts.anchor(0.5f, 0.5f)
            } else {
                if (it.eventGroup == null) {
                    icon = YELLOW_MARKER
                } else {
                    when (it.eventGroup) {
                        EventGroup.AVTOCESTA -> icon = GREEN_MARKER
                        EventGroup.HITRA_CESTA -> icon = AZURE_MARKER
                        EventGroup.MEJNI_PREHOD -> icon = ORANGE_MARKER
                        else -> icon = YELLOW_MARKER
                    }
                }
            }

            opts.icon(icon)
            opts.draggable(false)

            Pair(it.id.toInt(), opts)
        }
    }

    fun getCounterMarkers(counters : Observable<TrafficCounter>) : Observable<MarkerOptions> {
        return counters
            .filter { it.status != TrafficStatus.NO_DATA }
            .map {
                MarkerOptions()
                    .position(LatLng(it.lat.toDouble(), it.lng.toDouble()))
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .draggable(false)
                    .icon(TRAFFIC_DENSITY_BITMAPS[it.status.ordinal])
            }
    }

};


