package si.virag.promet.presenter

import si.virag.promet.PrometApplication
import si.virag.promet.model.TrafficData
import si.virag.promet.view.MapView

class MapPresenter(val view : MapView) {

    lateinit var trafficData : TrafficData

    init {
        PrometApplication.graph.inject(this)
    }


    fun onResume() {

    }

    fun onPause() {

    }

}