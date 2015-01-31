package si.virag.promet.api.push;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;
import si.virag.promet.gcm.RegistrationService;

@Module(
        injects = RegistrationService.class,
        complete = false
)
public class PushDataPrometApi implements Converter {

    private final RestAdapter adapter;

    public PushDataPrometApi() {
        adapter = new RestAdapter.Builder()
                .setEndpoint("http://prometapi.virag.si")
                .setConverter(this)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
    }

    @Singleton
    @Provides
    public PushApi getPushApi() {
        return adapter.create(PushApi.class);
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        byte[] buffer = new byte[(int)body.length()];

        try {
            body.in().read(buffer);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert response.", e);
        }

        return new String(buffer);
    }

    @Override
    public TypedOutput toBody(Object object) {
        return new TypedString((String) object);
    }
}
