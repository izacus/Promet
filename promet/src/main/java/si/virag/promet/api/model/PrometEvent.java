package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;


public class PrometEvent implements Comparable {

    @SerializedName("Id")
    public long id;

    @SerializedName("Cesta")
    public String roadName;

    @SerializedName("Title")
    public String cause;

    @SerializedName("Description")
    public String description;

    @SerializedName("x_wgs")
    public double lng;

    @SerializedName("y_wgs")
    public double lat;

    @SerializedName("Kategorija")
    public EventGroup eventGroup;

    @SerializedName("Updated")
    public ZonedDateTime updated;

    @SerializedName("VeljavnostOd")
    public ZonedDateTime validFrom;

    @SerializedName("VeljavnostDo")
    public ZonedDateTime validTo;

    @SerializedName("Prioriteta")
    public int priority;

    @SerializedName("PrioritetaCeste")
    public int roadPriority;

    @SerializedName("isMejniPrehod")
    public boolean isBorderCrossing;

    public boolean isHighPriority() {
        return "nesreča".equalsIgnoreCase(cause) || "zastoj".equalsIgnoreCase(cause);
    }

    public boolean isRoadworks() {
        return "Roadworks".equalsIgnoreCase(cause) || "Delo na cesti".equalsIgnoreCase(cause);
    }

    @Override
    public String toString() {
        return "PrometEvent{" +
                "cause='" + cause + '\'' +
                ", id=" + id +
                ", roadName='" + roadName + '\'' +
                ", description='" + description + '\'' +
                ", lng=" + lng +
                ", lat=" + lat +
                ", eventGroup=" + eventGroup +
                ", updated=" + updated +
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
