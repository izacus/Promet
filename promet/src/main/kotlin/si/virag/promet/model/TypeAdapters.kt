package si.virag.promet.model

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import si.virag.promet.model.data.EventGroup
import si.virag.promet.model.data.FunnyDouble
import si.virag.promet.model.data.TrafficStatus
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

class TrafficStatusTypeAdapter : TypeAdapter<TrafficStatus>() {

    override fun read(reader: JsonReader?): TrafficStatus {
        if (reader == null) return TrafficStatus.NO_DATA
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return TrafficStatus.NO_DATA
        }

        val value = reader.nextInt()
        if (value >= TrafficStatus.values().size) return TrafficStatus.NO_DATA
        return TrafficStatus.values()[value]
    }

    override fun write(writer: JsonWriter?, value: TrafficStatus?) {
        if (writer == null) return
        if (value == null) {
            writer.nullValue()
            return
        }

        writer.value(value.ordinal)
    }
}

class EventGroupTypeAdapter : TypeAdapter<EventGroup>() {
    val typeMapping = mapOf(Pair("AC", EventGroup.AVTOCESTA),
                            Pair("A1", EventGroup.AVTOCESTA),
                            Pair("HC", EventGroup.HITRA_CESTA),
                            Pair("G1", EventGroup.REGIONALNA_CESTA),
                            Pair("G2", EventGroup.REGIONALNA_CESTA),
                            Pair("R1", EventGroup.REGIONALNA_CESTA),
                            Pair("R2", EventGroup.REGIONALNA_CESTA),
                            Pair("R3", EventGroup.REGIONALNA_CESTA),
                            Pair("RT", EventGroup.REGIONALNA_CESTA),
                            Pair("LC", EventGroup.LOKALNA_CESTA),
                            Pair("JP", EventGroup.LOKALNA_CESTA),
                            Pair("LG", EventGroup.LOKALNA_CESTA),
                            Pair("LZ", EventGroup.LOKALNA_CESTA),
                            Pair("LK", EventGroup.LOKALNA_CESTA))


    override fun write(out: JsonWriter?, value: EventGroup?) {
        throw UnsupportedOperationException("Serializing this type is not supported.")
    }

    override fun read(reader: JsonReader?): EventGroup? {
        if (reader == null) return null
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        val str = reader.nextString();
        return typeMapping[str];
    }
}

class EpochDateTypeAdapter : TypeAdapter<LocalDateTime>() {


    override fun write(out: JsonWriter?, value: LocalDateTime?) {
        if (out == null) return
        if (value == null) {
            out.nullValue()
            return
        }

        out.value(value.toInstant(ZoneOffset.UTC).epochSecond)
    }

    override fun read(reader: JsonReader?): LocalDateTime? {
        if (reader == null) return null
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        val timestamp = reader.nextLong()

        // Sometimes API will be dumb and return value either in milliseconds or seconds. Unreliably. Yeah.
        val needsCorrection =  timestamp < 31539661000L
        return LocalDateTime.ofEpochSecond(if (needsCorrection) timestamp * 1000L else timestamp, 0, ZoneOffset.UTC)
    }
}

class FunnyDoubleAdapter : TypeAdapter<FunnyDouble>() {

    val commaFormat = NumberFormat.getInstance(Locale.FRANCE)
    val dotFormat = NumberFormat.getInstance(Locale.US)

    override fun read(reader: JsonReader?): FunnyDouble? {
        if (reader == null) return null
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        // Sometimes API will return decimals with commas and sometimes with dots. Unreliably. Yup.
        val doubleString = reader.nextString()
        try {
            if (doubleString.contains(",")) {
                return FunnyDouble(commaFormat.parse(doubleString).toDouble())
            } else {
                return FunnyDouble(dotFormat.parse(doubleString).toDouble())
            }
        } catch (e : ParseException) {
            return FunnyDouble(0.0)
        }
    }

    override fun write(out: JsonWriter?, value: FunnyDouble?) {
        if (out == null) return
        if (value == null) out.nullValue()
        out.value(if (value == null) 0.0 else value.toDouble() )
    }

}

