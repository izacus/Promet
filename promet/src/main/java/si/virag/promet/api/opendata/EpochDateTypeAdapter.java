package si.virag.promet.api.opendata;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;

public class EpochDateTypeAdapter extends TypeAdapter<DateTime> {
    @Override
    public void write(JsonWriter out, DateTime value) throws IOException {
        out.value(value.getMillis());
    }

    @Override
    public DateTime read(JsonReader in) throws IOException {

        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        return new DateTime(in.nextLong());
    }
}
