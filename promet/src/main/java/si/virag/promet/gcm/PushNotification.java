package si.virag.promet.gcm;

import io.realm.RealmObject;

public class PushNotification extends RealmObject {
    private long id;

    private String cause;
    private String causeEn;
    private String road;
    private String roadEn;
    private int roadPriority;
    private boolean isCrossing;

    private long created;
    private long validUntil;

    private String description;
    private String descrptionEn;

    private double lat;
    private double lng;

    public PushNotification() {
    }

    public PushNotification(long id, String cause, String causeEn, String road, String roadEn, String description, String descrptionEn, int roadPriority, boolean isCrossing, long created, long validUntil, double lat, double lng) {
        this.id = id;
        this.cause = cause;
        this.causeEn = causeEn;
        this.road = road;
        this.roadEn = roadEn;
        this.description = description;
        this.descrptionEn = descrptionEn;
        this.roadPriority = roadPriority;
        this.created = created;
        this.validUntil = validUntil;
        this.lat = lat;
        this.lng = lng;
        this.isCrossing = isCrossing;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getCauseEn() {
        return causeEn;
    }

    public void setCauseEn(String causeEn) {
        this.causeEn = causeEn;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getRoadEn() {
        return roadEn;
    }

    public void setRoadEn(String roadEn) {
        this.roadEn = roadEn;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(long validUntil) {
        this.validUntil = validUntil;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getRoadPriority() {
        return roadPriority;
    }

    public void setRoadPriority(int roadPriority) {
        this.roadPriority = roadPriority;
    }

    public boolean isCrossing() {
        return isCrossing;
    }

    public void setCrossing(boolean isCrossing) {
        this.isCrossing = isCrossing;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescrptionEn() {
        return descrptionEn;
    }

    public void setDescrptionEn(String descrptionEn) {
        this.descrptionEn = descrptionEn;
    }
}
