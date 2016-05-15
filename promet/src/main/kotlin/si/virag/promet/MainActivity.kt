package si.virag.promet

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.sdoward.rxgooglemap.MapObservableProvider
import kotlinx.android.synthetic.main.activity_main.*
import si.virag.promet.model.data.TrafficEvent
import si.virag.promet.presenter.MapPresenter
import si.virag.promet.view.MapView

class MainActivity : AppCompatActivity(), MapView {

    // Constants
    val MAP_CENTER : LatLng = LatLng(46.055556, 14.508333);

    val presenter = MapPresenter(this)
    lateinit var mapObservable : MapObservableProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapObservable = MapObservableProvider(main_maps as SupportMapFragment)
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
        mapObservable.mapReadyObservable
                     .subscribe { configureMap(it) }
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun showMarkers(events: List<TrafficEvent>) {

    }


    fun configureMap(map : GoogleMap) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTER, 10.0f));
        map.isTrafficEnabled = true
        map.isIndoorEnabled = false
        map.isBuildingsEnabled = false
        map.isMyLocationEnabled = true

        val uiSettings = map.uiSettings
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isCompassEnabled = true
        uiSettings.isMyLocationButtonEnabled = true
        uiSettings.isMapToolbarEnabled = false
    }
}