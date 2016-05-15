package si.virag.promet.view

import si.virag.promet.model.data.TrafficEvent

/**
 * View handling map data
 */
interface MapView {

    fun showMarkers(events: List<TrafficEvent>)

}