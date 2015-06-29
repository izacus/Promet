package si.virag.promet.api.opendata;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import si.virag.promet.api.model.TrafficStatus;

public class TrafficStatusAdapter extends TypeAdapter<TrafficStatus> {
    @Override
    public void write(JsonWriter out, TrafficStatus value) throws IOException {
        out.value(value.ordinal());
    }

    @Override
    public TrafficStatus read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return TrafficStatus.NO_DATA;
        }

        int value = in.nextInt();
        if (value >= TrafficStatus.values().length) return TrafficStatus.NO_DATA;
        return TrafficStatus.values()[value];
    }
}
