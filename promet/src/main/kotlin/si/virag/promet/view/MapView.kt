package si.virag.promet.view

import rx.Observable
import rx.Subscription
import si.virag.promet.model.data.TrafficCounter
import si.virag.promet.model.data.TrafficEvent

/**
 * View handling map data
 */
interface MapView {

    fun showMarkers(events: Observable<TrafficEvent>, counters: Observable<TrafficCounter>) : Subscription

}