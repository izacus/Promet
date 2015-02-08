package si.virag.promet.api.push;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.crashlytics.android.Crashlytics;

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

@Module
public class PushDataPrometApi implements Converter {

    private final RestAdapter adapter;

    public PushDataPrometApi(Context context) {
        int appVersion = 0;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
        }

        final String header = String.format("Promet/%d %s/%s/%s Android %s/%d (%s)", appVersion,
                Build.MANUFACTURER,
                Build.MODEL,
                Build.DEVICE,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                Build.FINGERPRINT);

        RequestInterceptor ri = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("User-Agent", header);
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
