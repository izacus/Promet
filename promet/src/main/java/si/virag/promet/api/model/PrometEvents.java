package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrometEvents {

    @SerializedName("Contents")
    public List<PrometEventsInternal> events;

    public static class PrometEventsInternal
    {
        @SerializedName("Data")
        public PrometEventsData data;
    }

    public static class PrometEventsData
    {
        @SerializedName("Items")
        public List<PrometEvent> events;
    }
}
