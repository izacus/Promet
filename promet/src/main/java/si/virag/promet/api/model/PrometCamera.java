package si.virag.promet.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PrometCamera implements Parcelable {

    @SerializedName("location_id")
    public String id;

    @SerializedName("x_wgs")
    public double lng;

    @SerializedName("y_wgs")
    public double lat;

    @SerializedName("region")
    public String region;

    @SerializedName("text")
    public String text;

    @SerializedName("image_url")
    public String imageUrl;

    public String getImageLink() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }

    public String getRegion() {
        return region;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeDouble(this.lng);
        dest.writeDouble(this.lat);
        dest.writeString(this.text);
        dest.writeString(this.region);
        dest.writeString(this.imageUrl);
    }

    protected PrometCamera(Parcel in) {
        this.id = in.readString();
        this.lng = in.readDouble();
        this.lat = in.readDouble();
        this.text = in.readString();
        this.region = in.readString();
        this.imageUrl = in.readString();
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
