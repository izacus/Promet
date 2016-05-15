package si.virag.promet.model.data

import com.google.gson.annotations.SerializedName

import org.threeten.bp.LocalDateTime

class TrafficEvent {

    @SerializedName("id")
    var id: Long = 0

    @SerializedName("cesta")
    var roadName: String = ""

    @SerializedName("vzrok")
    var cause: String = ""

    @SerializedName("vzrokEn")
    var causeEn: String = ""

    @SerializedName("opis")
    var description: String = ""

    @SerializedName("opisEn")
    var descriptionEn: String = ""

    @SerializedName("x_wgs")
    var lng: Double = 0.0

    @SerializedName("y_wgs")
    var lat: Double = 0.0

    @SerializedName("kategorija")
    var eventGroup: EventGroup = EventGroup.LOKALNA_CESTA

    @SerializedName("vneseno")
    var entered: LocalDateTime? = null

    @SerializedName("veljavnostOd")
    var validFrom: LocalDateTime? = null

    @SerializedName("veljavnostDo")
    var validTo: LocalDateTime? = null

    @SerializedName("prioriteta")
    var priority: Int = 0

    @SerializedName("prioritetaCeste")
    var roadPriority: Int = 0

    @SerializedName("isMejniPrehod")
    var isBorderCrossing: Boolean = false

    val isHighPriority: Boolean
        get() = "nesreča".equals(cause, ignoreCase = true) || "zastoj".equals(cause, ignoreCase = true)

    val isRoadworks: Boolean
        get() = "Roadworks".equals(causeEn, ignoreCase = true) || "Delo na cesti".equals(cause, ignoreCase = true)

}
