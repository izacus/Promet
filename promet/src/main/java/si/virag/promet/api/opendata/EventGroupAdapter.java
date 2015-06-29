package si.virag.promet.api.opendata;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import si.virag.promet.api.model.EventGroup;

public class EventGroupAdapter extends TypeAdapter<EventGroup> {

    public static Map<String, EventGroup> typeMapping = new HashMap<>();


    static {
        typeMapping.put("AC", EventGroup.AVTOCESTA);
        typeMapping.put("A1", EventGroup.AVTOCESTA);
        typeMapping.put("HC", EventGroup.HITRA_CESTA);
        typeMapping.put("G1", EventGroup.REGIONALNA_CESTA);
        typeMapping.put("G2", EventGroup.REGIONALNA_CESTA);
        typeMapping.put("R1", EventGroup.REGIONALNA_CESTA);
        typeMapping.put("R2", EventGroup.REGIONALNA_CESTA);
        typeMapping.put("R3", EventGroup.REGIONALNA_CESTA);
        typeMapping.put("RT", EventGroup.REGIONALNA_CESTA);
        typeMapping.put("LC", EventGroup.LOKALNA_CESTA);
        typeMapping.put("JP", EventGroup.LOKALNA_CESTA);
        typeMapping.put("LG", EventGroup.LOKALNA_CESTA);
        typeMapping.put("LZ", EventGroup.LOKALNA_CESTA);
        typeMapping.put("LK", EventGroup.LOKALNA_CESTA);
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
        return typeMapping.get(str);
    }
}
