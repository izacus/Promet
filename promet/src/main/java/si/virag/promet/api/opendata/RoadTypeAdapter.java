package si.virag.promet.api.opendata;

import com.google.common.collect.ImmutableMap;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import si.virag.promet.api.model.RoadType;

import java.io.IOException;

public class RoadTypeAdapter extends TypeAdapter<RoadType> {

    public static ImmutableMap<String, RoadType> typeMapping = ImmutableMap.<String, RoadType>builder()
                        .put("AC", RoadType.AVTOCESTA)
                        .put("A1", RoadType.AVTOCESTA)
                        .put("HC", RoadType.HITRA_CESTA)
                        .put("G1", RoadType.REGIONALNA_CESTA)
                        .put("G2", RoadType.REGIONALNA_CESTA)
                        .put("R1", RoadType.REGIONALNA_CESTA)
                        .put("R2", RoadType.REGIONALNA_CESTA)
                        .put("R3", RoadType.REGIONALNA_CESTA)
                        .put("RT", RoadType.REGIONALNA_CESTA)
                        .put("LC", RoadType.LOKALNA_CESTA)
                        .put("JP", RoadType.LOKALNA_CESTA)
                        .put("LG", RoadType.LOKALNA_CESTA)
                        .put("LZ", RoadType.LOKALNA_CESTA)
                        .put("LK", RoadType.LOKALNA_CESTA)
                        .build();


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
