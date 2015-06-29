package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrometCounters {

    @SerializedName("feed")
    public PrometCountersInternal counters;

    public static class PrometCountersInternal {
        @SerializedName("updated")
        public long lastUpdate;

        @SerializedName("entry")
        public List<PrometCounter> counters;
    }
}
