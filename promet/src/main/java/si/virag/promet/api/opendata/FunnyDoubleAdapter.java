package si.virag.promet.api.opendata;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class FunnyDoubleAdapter extends TypeAdapter<Double> {

    private static final NumberFormat commaFormat = NumberFormat.getInstance(Locale.FRANCE);
    private static final NumberFormat dotFormat = NumberFormat.getInstance(Locale.US);

    @Override
    public void write(JsonWriter out, Double value) throws IOException {
        out.value(value.doubleValue());
    }

    @Override
    public Double read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL)
            return 0.0;

        String doubleString = in.nextString();

        try {
            if (doubleString.contains(",")) {
                return commaFormat.parse(doubleString).doubleValue();
            } else {
                return dotFormat.parse(doubleString).doubleValue();
            }
        } catch (ParseException e) {
            return 0.0;
        }

    }
}
