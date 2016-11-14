package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrometCameras {

    @SerializedName("Contents")
    public List<CamerasFeed> feed;

    public static class CamerasFeed {

        @SerializedName("Data")
        public CameraData data;
    }

    public static class CameraData {
        @SerializedName("Items")
        public List<PrometCamera> cameras;
    }
}
