package si.virag.promet.api.opendata;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;


import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import java.io.IOException;

public class EpochDateTypeAdapter extends TypeAdapter<LocalDateTime> {
    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        out.value(value.toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {

        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        // Sometimes the API sends time that requires * 1000, sometimes not. Compare to 1971 epoch and decide.
        final long timestamp = in.nextLong();
        boolean needsCorrection =  timestamp < 31539661000l;
        Instant instant = Instant.ofEpochMilli(needsCorrection ? timestamp * 1000l : timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
