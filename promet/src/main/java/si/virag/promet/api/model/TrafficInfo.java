package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TrafficInfo {

    @SerializedName("events")
    public List<PrometEvent> events;

    @SerializedName("cameras")
    public List<PrometCamera> cameras;

}
