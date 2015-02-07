package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class PrometEvent {

    @SerializedName("id")
    public long id;

    @SerializedName("cesta")
    public String roadName;

    @SerializedName("vzrok")
    public String cause;

    @SerializedName("vzrokEn")
    public String causeEn;

    @SerializedName("opis")
    public String description;

    @SerializedName("opisEn")
    public String descriptionEn;

    @SerializedName("x_wgs")
    public double lng;

    @SerializedName("y_wgs")
    public double lat;

    @SerializedName("kategorija")
    public RoadType roadType;

    @SerializedName("vneseno")
    public Date entered;

    @SerializedName("prioriteta")
    public int priority;

    @SerializedName("prioritetaCeste")
    public int roadPriority;

    @SerializedName("isMejniPrehod")
    public boolean isBorderCrossing;

    public boolean isHighPriority() {
        return "nesreča".equalsIgnoreCase(cause) || "zastoj".equalsIgnoreCase(cause);
    }
}
