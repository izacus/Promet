package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.Date;

public class PrometCounter {

    @SerializedName("id")
    public String id;

    @SerializedName("stevci_geoY_wgs")
    public double lat;

    @SerializedName("stevci_geoX_wgs")
    public double lng;

    @SerializedName("updated")
    public DateTime updated;

    @SerializedName("stevci_occ")
    // Occupancy in % * 100 (e.g. 84 = 8.4%)
    public int occupancy;

    @SerializedName("stevci_stev")
    // Vehicles per hour
    public int numVehicles;

    @SerializedName("stevci_stat")
    public TrafficStatus status;

    @SerializedName("stevci_hit")
    public int avgSpeed;

    @SerializedName("stevci_lokacijaOpis")
    public String locationName;

    @SerializedName("stevci_gap")
    public Double gap;

    @Override
    public String toString() {
        return "PrometCounter{" +
                "avgSpeed=" + avgSpeed +
                ", id='" + id + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", updated=" + updated +
                ", occupancy=" + occupancy +
                ", numVehicles=" + numVehicles +
                ", status=" + status +
                ", locationName='" + locationName + '\'' +
                ", gap=" + gap +
                '}';
    }
}
