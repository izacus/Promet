package si.virag.promet.api.opendata;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import si.virag.promet.api.model.EventGroup;

public class EventGroupAdapter extends TypeAdapter<EventGroup> {

    private static final Map<String, EventGroup> TYPE_MAPPING;

    static {
        Map<String, EventGroup> mapping = new HashMap<>();
        mapping.put("AC", EventGroup.AVTOCESTA);
        mapping.put("A1", EventGroup.AVTOCESTA);
        mapping.put("HC", EventGroup.HITRA_CESTA);
        mapping.put("G1", EventGroup.REGIONALNA_CESTA);
        mapping.put("G2", EventGroup.REGIONALNA_CESTA);
        mapping.put("R1", EventGroup.REGIONALNA_CESTA);
        mapping.put("R2", EventGroup.REGIONALNA_CESTA);
        mapping.put("R3", EventGroup.REGIONALNA_CESTA);
        mapping.put("RT", EventGroup.REGIONALNA_CESTA);
        mapping.put("LC", EventGroup.LOKALNA_CESTA);
        mapping.put("JP", EventGroup.LOKALNA_CESTA);
        mapping.put("LG", EventGroup.LOKALNA_CESTA);
        mapping.put("LZ", EventGroup.LOKALNA_CESTA);
        mapping.put("LK", EventGroup.LOKALNA_CESTA);
        TYPE_MAPPING = Collections.unmodifiableMap(mapping);
    }

    @Override
    public void write(JsonWriter out, EventGroup value) throws IOException {
        throw new RuntimeException("Serializing this class is not supported.");
    }

    @Override
    public EventGroup read(JsonReader in) throws IOException {

        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String str = in.nextString();
        return TYPE_MAPPING.get(str);
    }
}
