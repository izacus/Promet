package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.temporal.ChronoUnit;


public class PrometEvent implements Comparable {

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
    public EventGroup eventGroup;

    @SerializedName("vneseno")
    public LocalDateTime entered;

    @SerializedName("veljavnostOd")
    public LocalDateTime validFrom;

    @SerializedName("veljavnostDo")
    public LocalDateTime validTo;

    @SerializedName("prioriteta")
    public int priority;

    @SerializedName("prioritetaCeste")
    public int roadPriority;

    @SerializedName("isMejniPrehod")
    public boolean isBorderCrossing;

    public boolean isHighPriority() {
        return "nesreča".equalsIgnoreCase(cause) || "zastoj".equalsIgnoreCase(cause);
    }

    public boolean isRoadworks() {
        return "Roadworks".equalsIgnoreCase(causeEn) || "Delo na cesti".equalsIgnoreCase(cause);
    }

    @Override
    public String toString() {
        return "PrometEvent{" +
                "cause='" + cause + '\'' +
                ", id=" + id +
                ", roadName='" + roadName + '\'' +
                ", causeEn='" + causeEn + '\'' +
                ", description='" + description + '\'' +
                ", descriptionEn='" + descriptionEn + '\'' +
                ", lng=" + lng +
                ", lat=" + lat +
                ", eventGroup=" + eventGroup +
                ", entered=" + entered +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", priority=" + priority +
                ", roadPriority=" + roadPriority +
                ", isBorderCrossing=" + isBorderCrossing +
                '}';
    }

    @Override
    public int compareTo(Object another) {
        if (!(another instanceof PrometEvent)) return 1;
        PrometEvent other = (PrometEvent)another;
        if (other.validFrom == null) return 1;
        if (validFrom == null) return -1;
        return (int)other.validFrom.until(validFrom, ChronoUnit.MILLIS);
    }
}
