package si.virag.promet

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
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
import si.virag.promet.settings.PrometSettings
import si.virag.promet.view.MapMarkerManager
import si.virag.promet.view.MapView
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MapView, PrometSettings.OnSettingsChangedListener {

    // Constants
    val MAP_CENTER : LatLng = LatLng(46.055556, 14.508333);

    val presenter = MapPresenter(this)
    val markerManager = MapMarkerManager(this)

    @Inject
    lateinit var settings : PrometSettings

    lateinit var mapObservable : MapObservableProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PrometApplication.graph.inject(this)
        main_maps.onCreate(savedInstanceState)
        mapObservable = MapObservableProvider(main_maps)
    }

    override fun onResume() {
        super.onResume()
        main_maps.onResume()
        presenter.onResume()
        mapObservable.mapReadyObservable
                     .subscribe { configureMap(it) }
        settings.registerChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        main_maps.onPause()
        presenter.onPause()
        settings.unregisterChangeListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        main_maps.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu?.findItem(R.id.menu_map_avtoceste)?.isChecked = settings.showHighways
        menu?.findItem(R.id.menu_map_crossings)?.isChecked = settings.showBorderCrossings
        menu?.findItem(R.id.menu_map_lokalne_ceste)?.isChecked = settings.showLocalRoads
        menu?.findItem(R.id.menu_map_regionalne_ceste)?.isChecked = settings.showRegionalRoads
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null || !item.isCheckable) return false
        val newStatus = !item.isChecked
        item.isChecked = newStatus

        when (item.itemId) {
            R.id.menu_map_avtoceste -> settings.showHighways = newStatus
            R.id.menu_map_crossings -> settings.showBorderCrossings = newStatus
            R.id.menu_map_regionalne_ceste -> settings.showRegionalRoads = newStatus
            R.id.menu_map_lokalne_ceste -> settings.showLocalRoads = newStatus
        }

        return true
    }

    override fun onSettingsChanged() {
        supportInvalidateOptionsMenu()
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
        val mapReadyClear = mapObservable.mapReadyObservable
                                         .map { it.clear(); it }

        val compositeSubscription = CompositeSubscription()
        compositeSubscription.add(markerStream.withLatestFrom(mapReadyClear, { marker, map -> Pair(marker, map) })
                                              .onBackpressureBuffer()
                                              .observeOn(AndroidSchedulers.mainThread())
                                              .subscribe { it.second.addMarker(it.first.second) })

        compositeSubscription.add(counterStream.withLatestFrom(mapReadyClear, { marker, map -> Pair(marker, map) })
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