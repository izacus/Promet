package si.virag.promet.model.data

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

class TrafficCounter {

    @SerializedName("id")
    var id : String = ""

    @SerializedName("stevci_geoY_wgs")
    var lat : FunnyDouble = FunnyDouble(0.0)
    @SerializedName("stevci_geoX_wgs")
    var lng : FunnyDouble = FunnyDouble(0.0)
    @SerializedName("updated")
    var updated : LocalDateTime? = null
    @SerializedName("stevci_occ")
    var occupancy : Int = 0
    @SerializedName("stevci_stev")
    var numVehicles : Int = 0
    @SerializedName("stevci_stat")
    var status : TrafficStatus = TrafficStatus.NO_DATA
    @SerializedName("stevci_hit")
    var avgSpeed : Int = 0
    @SerializedName("stevci_lokacijaOpis")
    var locationName : String = ""
    @SerializedName("stevci_gap")
    var gap : FunnyDouble = FunnyDouble(0.0)

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TrafficCounter

        if (id != other.id) return false
        if (lat != other.lat) return false
        if (lng != other.lng) return false
        if (updated != other.updated) return false
        if (occupancy != other.occupancy) return false
        if (numVehicles != other.numVehicles) return false
        if (status != other.status) return false
        if (avgSpeed != other.avgSpeed) return false
        if (locationName != other.locationName) return false
        if (gap != other.gap) return false

        return true
    }

    override fun hashCode(): Int{
        var result = id.hashCode()
        result += 31 * result + lat.hashCode()
        result += 31 * result + lng.hashCode()
        result += 31 * result + (updated?.hashCode() ?: 0)
        result += 31 * result + occupancy
        result += 31 * result + numVehicles
        result += 31 * result + status.hashCode()
        result += 31 * result + avgSpeed
        result += 31 * result + locationName.hashCode()
        result += 31 * result + gap.hashCode()
        return result
    }
}