package si.virag.promet

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.sdoward.rxgooglemap.MapObservableProvider
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func2
import rx.subscriptions.CompositeSubscription
import si.virag.promet.model.data.TrafficCounter
import si.virag.promet.model.data.TrafficEvent
import si.virag.promet.presenter.MapPresenter
import si.virag.promet.view.MapMarkerManager
import si.virag.promet.view.MapView

class MainActivity : AppCompatActivity(), MapView {

    // Constants
    val MAP_CENTER : LatLng = LatLng(46.055556, 14.508333);

    val presenter = MapPresenter(this)
    val markerManager = MapMarkerManager(this)

    lateinit var mapObservable : MapObservableProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_maps.onCreate(savedInstanceState)
        mapObservable = MapObservableProvider(main_maps)
    }

    override fun onResume() {
        super.onResume()
        main_maps.onResume()
        presenter.onResume()
        mapObservable.mapReadyObservable
                     .subscribe { configureMap(it) }
    }

    override fun onPause() {
        super.onPause()
        main_maps.onPause()
        presenter.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        main_maps.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        main_maps.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        main_maps.onDestroy()
    }

    override fun showMarkers(events: Observable<TrafficEvent>, counters: Observable<TrafficCounter>) : Subscription {
        val markerStream = markerManager.getEventMarkers(events)
        val counterStream = markerManager.getCounterMarkers(counters)

        val compositeSubscription = CompositeSubscription()
        compositeSubscription.add(markerStream.withLatestFrom(mapObservable.mapReadyObservable, { marker, map -> Pair(marker, map) })
                                              .onBackpressureBuffer()
                                              .observeOn(AndroidSchedulers.mainThread())
                                              .subscribe { it.second.addMarker(it.first.second) })

        compositeSubscription.add(counterStream.withLatestFrom(mapObservable.mapReadyObservable, { marker, map -> Pair(marker, map) })
                                               .onBackpressureBuffer()
                                               .observeOn(AndroidSchedulers.mainThread())
                                               .subscribe { it.second.addMarker(it.first) })
        return compositeSubscription
    }


    fun configureMap(map : GoogleMap) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTER, 10.0f));
        map.isTrafficEnabled = true
        map.isIndoorEnabled = false
        map.isBuildingsEnabled = false
        map.isMyLocationEnabled = false

        val uiSettings = map.uiSettings
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isCompassEnabled = true
        uiSettings.isMyLocationButtonEnabled = true
        uiSettings.isMapToolbarEnabled = false
    }
}