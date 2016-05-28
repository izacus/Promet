package si.virag.promet.model

import org.threeten.bp.LocalDateTime
import si.virag.promet.model.data.EventGroup
import si.virag.promet.model.data.TrafficCounter
import si.virag.promet.model.data.TrafficEvent
import si.virag.promet.model.data.TrafficStatus
import si.virag.promet.settings.PrometSettings

fun filterEventsAccordingToSettings(settings: PrometSettings, trafficEvent: TrafficEvent): Boolean {
    if (trafficEvent.eventGroup == null) return true
    if (trafficEvent.validTo?.isBefore(LocalDateTime.now()) ?: false) return false

    when (trafficEvent.eventGroup) {
        EventGroup.AVTOCESTA, EventGroup.HITRA_CESTA -> return settings.showHighways
        EventGroup.MEJNI_PREHOD -> return settings.showBorderCrossings
        EventGroup.REGIONALNA_CESTA -> return settings.showRegionalRoads
        EventGroup.LOKALNA_CESTA -> return settings.showLocalRoads
    }

    return true
}

fun filterCounters(counter : TrafficCounter): Boolean {
    return counter.status != TrafficStatus.NORMAL_TRAFFIC && counter.status != TrafficStatus.NO_DATA;
}