package si.virag.promet.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrometCounters {

    @SerializedName("Contents")
    public List<PrometCountersInternal> counters;

    public static class PrometCountersInternal {
        @SerializedName("Data")
        public PrometCountersData data;
    }

    public static class PrometCountersData {
        @SerializedName("Items")
        public List<PrometCounter> counters;
    }
}
