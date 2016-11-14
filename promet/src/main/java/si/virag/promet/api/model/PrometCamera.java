package si.virag.promet.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrometCamera implements Parcelable {

    @SerializedName("Id")
    public String id;

    @SerializedName("Title")
    public String title;

    @SerializedName("x_wgs")
    public double lng;

    @SerializedName("y_wgs")
    public double lat;

    @SerializedName("Kamere")
    public List<Camera> cameras;

    public String getImageLink() {
        return cameras == null || cameras.size() == 0 ? null : cameras.get(0).imageLink;
    }

    public String getText() {
        return cameras == null || cameras.size() == 0 ? null : cameras.get(0).text;
    }

    public String getRegion() {
        return cameras == null || cameras.size() == 0 ? null : cameras.get(0).region;
    }

    public static class Camera implements Parcelable {
        @SerializedName("Text")
        public String text;
        @SerializedName("Region")
        public String region;
        @SerializedName("Image")
        public String imageLink;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.text);
            dest.writeString(this.region);
            dest.writeString(this.imageLink);
        }

        protected Camera(Parcel in) {
            this.text = in.readString();
            this.region = in.readString();
            this.imageLink = in.readString();
        }

        public static final Creator<Camera> CREATOR = new Creator<Camera>() {
            @Override
            public Camera createFromParcel(Parcel source) {
                return new Camera(source);
            }

            @Override
            public Camera[] newArray(int size) {
                return new Camera[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeDouble(this.lng);
        dest.writeDouble(this.lat);
        dest.writeTypedList(this.cameras);
    }

    public PrometCamera() {
    }

    protected PrometCamera(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.lng = in.readDouble();
        this.lat = in.readDouble();
        this.cameras = in.createTypedArrayList(Camera.CREATOR);
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
