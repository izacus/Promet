package si.virag.promet.api.opendata;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import si.virag.promet.api.model.RoadType;

public class RoadTypeAdapter extends TypeAdapter<RoadType> {

    public static Map<String, RoadType> typeMapping = new HashMap<>();


    static {
        typeMapping.put("AC", RoadType.AVTOCESTA);
        typeMapping.put("A1", RoadType.AVTOCESTA);
        typeMapping.put("HC", RoadType.HITRA_CESTA);
        typeMapping.put("G1", RoadType.REGIONALNA_CESTA);
        typeMapping.put("G2", RoadType.REGIONALNA_CESTA);
        typeMapping.put("R1", RoadType.REGIONALNA_CESTA);
        typeMapping.put("R2", RoadType.REGIONALNA_CESTA);
        typeMapping.put("R3", RoadType.REGIONALNA_CESTA);
        typeMapping.put("RT", RoadType.REGIONALNA_CESTA);
        typeMapping.put("LC", RoadType.LOKALNA_CESTA);
        typeMapping.put("JP", RoadType.LOKALNA_CESTA);
        typeMapping.put("LG", RoadType.LOKALNA_CESTA);
        typeMapping.put("LZ", RoadType.LOKALNA_CESTA);
        typeMapping.put("LK", RoadType.LOKALNA_CESTA);
    }

    @Override
    public void write(JsonWriter out, RoadType value) throws IOException {
        throw new RuntimeException("Serializing this class is not supported.");
    }

    @Override
    public RoadType read(JsonReader in) throws IOException {

        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String str = in.nextString();
        return typeMapping.get(str);
    }
}
