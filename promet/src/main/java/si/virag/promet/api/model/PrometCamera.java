package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

public class PrometCamera {

    @SerializedName("id")
    public int id;

    @SerializedName("title")
    public String title;

    @SerializedName("summary")
    public String summary;

    @SerializedName("kazipot_region")
    public String region;

    @SerializedName("kazipot_group")
    public String group;

    @SerializedName("link")
    public String imageLink;

    @SerializedName("x_wgs")
    public double lng;

    @SerializedName("y_wgs")
    public double lat;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrometCamera that = (PrometCamera) o;

        if (id != that.id) return false;
        if (Double.compare(that.lng, lng) != 0) return false;
        if (Double.compare(that.lat, lat) != 0) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (summary != null ? !summary.equals(that.summary) : that.summary != null) return false;
        if (region != null ? !region.equals(that.region) : that.region != null) return false;
        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        return imageLink != null ? imageLink.equals(that.imageLink) : that.imageLink == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (region != null ? region.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (imageLink != null ? imageLink.hashCode() : 0);
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "PrometCamera{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", region='" + region + '\'' +
                ", group='" + group + '\'' +
                ", imageLink='" + imageLink + '\'' +
                ", lng=" + lng +
                ", lat=" + lat +
                '}';
    }
}
