package si.virag.promet.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PrometCamera implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.summary);
        dest.writeString(this.region);
        dest.writeString(this.group);
        dest.writeString(this.imageLink);
        dest.writeDouble(this.lng);
        dest.writeDouble(this.lat);
    }

    protected PrometCamera(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.summary = in.readString();
        this.region = in.readString();
        this.group = in.readString();
        this.imageLink = in.readString();
        this.lng = in.readDouble();
        this.lat = in.readDouble();
    }

    public static final Parcelable.Creator<PrometCamera> CREATOR = new Parcelable.Creator<PrometCamera>() {
        @Override
        public PrometCamera createFromParcel(Parcel source) {
            return new PrometCamera(source);
        }

        @Override
        public PrometCamera[] newArray(int size) {
            return new PrometCamera[size];
        }
    };
}
