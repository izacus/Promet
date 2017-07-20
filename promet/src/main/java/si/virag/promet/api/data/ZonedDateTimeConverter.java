package si.virag.promet.api.data;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.TemporalQuery;

import java.io.IOException;

public class ZonedDateTimeConverter extends TypeAdapter<ZonedDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final TemporalQuery<ZonedDateTime> query = new TemporalQuery<ZonedDateTime>() {
        @Override
        public ZonedDateTime queryFrom(TemporalAccessor temporalAccessor) {
            return ZonedDateTime.from(temporalAccessor);
        }
    };

    @Override
    public void write(JsonWriter out, ZonedDateTime value) throws IOException {

    }

    @Override
    public ZonedDateTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) return null;
        String str = in.nextString();
        return formatter.parse(str, query);
    }
}
