package si.virag.promet.presenter

import android.util.Log
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import si.virag.promet.PrometApplication
import si.virag.promet.model.TrafficData
import si.virag.promet.view.MapView
import javax.inject.Inject

class MapPresenter(val view : MapView) {
    val LOG_TAG = "Promet.Map"

    var trafficDataSubscription : Subscription? = null

    @Inject
    lateinit var trafficData : TrafficData

    init {
        PrometApplication.graph.inject(this)
    }

    fun onResume() {
        val trafficEvents = trafficData.getTrafficEvents()
        val trafficCounters = trafficData.getTrafficCounters()
        trafficDataSubscription = view.showMarkers(trafficEvents, trafficCounters)
    }

    fun onPause() {
        trafficDataSubscription?.unsubscribe()
    }

}