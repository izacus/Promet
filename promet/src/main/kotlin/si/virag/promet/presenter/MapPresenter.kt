package si.virag.promet.presenter

import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import si.virag.promet.PrometApplication
import si.virag.promet.model.TrafficData
import si.virag.promet.model.filterCounters
import si.virag.promet.model.filterEventsAccordingToSettings
import si.virag.promet.settings.PrometSettings
import si.virag.promet.view.MapView
import javax.inject.Inject

class MapPresenter(val view : MapView) : PrometSettings.OnSettingsChangedListener {

    val LOG_TAG = "Promet.Map"

    var trafficDataSubscription : Subscription? = null

    @Inject
    lateinit var trafficData : TrafficData

    @Inject
    lateinit var settings : PrometSettings

    init {
        PrometApplication.graph.inject(this)
    }

    fun onResume() {
        updateDisplayedData()
        settings.registerChangeListener(this)
    }

    fun onPause() {
        settings.unregisterChangeListener(this)
        trafficDataSubscription?.unsubscribe()
    }

    override fun onSettingsChanged() {
        updateDisplayedData()
    }

    fun updateDisplayedData() {
        val trafficEvents = trafficData.getTrafficEvents()
                                       .filter { filterEventsAccordingToSettings(settings, it) }
        val trafficCounters = trafficData.getTrafficCounters()
                                         .filter { filterCounters(it) }
        trafficDataSubscription = view.showMarkers(trafficEvents, trafficCounters)
    }

}