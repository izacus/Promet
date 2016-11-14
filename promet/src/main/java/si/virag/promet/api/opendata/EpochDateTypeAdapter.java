package si.virag.promet.api.opendata;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;

public class EpochDateTypeAdapter extends TypeAdapter<ZonedDateTime> {
    @Override
    public void write(JsonWriter out, ZonedDateTime value) throws IOException {
        out.value(value.toInstant().toEpochMilli());
    }

    @Override
    public ZonedDateTime read(JsonReader in) throws IOException {

        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        return ZonedDateTime.parse(in.nextString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
