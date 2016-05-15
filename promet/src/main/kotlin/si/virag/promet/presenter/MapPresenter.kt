package si.virag.promet.presenter

import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import si.virag.promet.PrometApplication
import si.virag.promet.model.TrafficData
import si.virag.promet.view.MapView
import javax.inject.Inject

class MapPresenter(val view : MapView) {

    var trafficDataSubscription : Subscription? = null

    @Inject
    lateinit var trafficData : TrafficData

    init {
        PrometApplication.graph.inject(this)
    }

    fun onResume() {
        trafficDataSubscription = view.showMarkers(trafficData.getTrafficEvents())
    }

    fun onPause() {
        trafficDataSubscription?.unsubscribe()
    }

}