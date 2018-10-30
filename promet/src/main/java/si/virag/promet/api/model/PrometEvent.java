package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import si.virag.promet.utils.DataUtils;


public class PrometEvent implements Comparable {

    @SerializedName("id")
    public long id;

    @SerializedName("road_sl")
    public String roadNameSl;

    @SerializedName("road_en")
    public String roadNameEn;

    @SerializedName("cause_sl")
    public String causeSl;

    @SerializedName("cause_en")
    public String causeEn;

    @SerializedName("description_sl")
    public String descriptionSl;

    @SerializedName("description_en")
    public String descriptionEn;

    @SerializedName("x_wgs")
    public double lng;

    @SerializedName("y_wgs")
    public double lat;

    @SerializedName("category")
    public EventGroup eventGroup;

    @SerializedName("updated")
    public ZonedDateTime updated;

    @SerializedName("valid_from")
    public ZonedDateTime validFrom;

    @SerializedName("valid_to")
    public ZonedDateTime validTo;

    @SerializedName("priority")
    public int priority;

    @SerializedName("road_priority")
    public int roadPriority;

    @SerializedName("is_border_crossing")
    public boolean isBorderCrossing;

    public boolean isHighPriority() {
        return DataUtils.isHighPriorityCause(causeSl);
    }

    public boolean isRoadworks() {
        return "Roadworks".equalsIgnoreCase(causeSl) || "Delo na cesti".equalsIgnoreCase(causeSl);
    }

    @Override
    public String toString() {
        return "PrometEvent{" +
                "id=" + id +
                ", roadNameSl='" + roadNameSl + '\'' +
                ", roadNameEn='" + roadNameEn + '\'' +
                ", causeSl='" + causeSl + '\'' +
                ", causeEn='" + causeEn + '\'' +
                ", descriptionSl='" + descriptionSl + '\'' +
                ", descriptionEn='" + descriptionEn + '\'' +
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
