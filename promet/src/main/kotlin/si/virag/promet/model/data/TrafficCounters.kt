package si.virag.promet.model.data

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

/**
 * This class works around strange REST structure
 */
class TrafficCounters {

    @SerializedName("feed")
    var counterList : TrafficCountersInternal = TrafficCountersInternal()

    fun updated() : LocalDateTime? {
        return counterList.lastUpdate
    }

    fun counters() : List<TrafficCounter> {
        return counterList.counters
    }

    class TrafficCountersInternal {
        @SerializedName("updated")
        var lastUpdate : LocalDateTime? = null
        @SerializedName("entry")
        var counters : List<TrafficCounter> = emptyList()

    }

}