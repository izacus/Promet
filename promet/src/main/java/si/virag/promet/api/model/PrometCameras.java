package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrometCameras {

    @SerializedName("feed")
    public CamerasFeed feed;

    public static class CamerasFeed {

        @SerializedName("entry")
        public List<PrometCamera> cameras;
    }
}
