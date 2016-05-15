package si.virag.promet.model.data

import com.google.gson.annotations.SerializedName

/**
 * This class works around the strange REST structure of the upstream API.
 */
class TrafficEvents {

    @SerializedName("dogodki")
    var eventList : PrometEventsInternal = PrometEventsInternal()

    fun events() : List<TrafficEvent> {
        return eventList.events
    }

    class PrometEventsInternal {
        @SerializedName("dogodek")
        var events : List<TrafficEvent> = emptyList()
    }
}