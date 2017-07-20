package si.virag.promet.api.push;

import android.content.Context;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;
import si.virag.promet.utils.DataUtils;

@Module
public class PushDataPrometApi implements Converter {

    private final RestAdapter adapter;

    public PushDataPrometApi(final Context context) {
        final String userAgent = DataUtils.getUserAgent(context);
        RequestInterceptor ri = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("User-Agent", userAgent);
            }
        };

        adapter = new RestAdapter.Builder()
                .setEndpoint("http://prometapi.virag.si")
                .setConverter(this)
                .setRequestInterceptor(ri)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
    }

    @Singleton
    @Provides
    public PrometPushApi getPushApi() {
        return adapter.create(PrometPushApi.class);
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
