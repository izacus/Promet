package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class PrometEvents {

    @SerializedName("dogodki")
    public PrometEventsInternal events;

    public static class PrometEventsInternal
    {
        @SerializedName("lastUpdate")
        public Date lastUpdate;

        @SerializedName("dogodek")
        public List<PrometEvent> events;

        // TODO more
    }
}
