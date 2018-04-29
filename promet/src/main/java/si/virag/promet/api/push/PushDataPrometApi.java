package si.virag.promet.api.push;

import android.content.Context;
import android.os.Build;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import si.virag.promet.BuildConfig;
import si.virag.promet.utils.DataUtils;

@Module
public class PushDataPrometApi {

    private final Retrofit adapter;

    public PushDataPrometApi(final Context context) {
        final String userAgent = DataUtils.getUserAgent(context);
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .header("User-Agent", userAgent)
                        .method(chain.request().method(), chain.request().body())
                        .build();

                return chain.proceed(request);
            }
        });

        if (BuildConfig.DEBUG) {
            client.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }

        String protocol = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP ? "https" : "http";

        adapter = new Retrofit.Builder()
                .baseUrl(protocol + "://prometapi.virag.si")
                .client(client.build())
                .addConverterFactory(new PushConverterFactory())
                .build();
    }

    @Singleton
    @Provides
    public PrometPushApi getPushApi() {
        return adapter.create(PrometPushApi.class);
    }

    private static class PushConverterFactory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, String> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return new Converter<ResponseBody, String>() {
                @Override
                public String convert(ResponseBody value) throws IOException {
                    return new String(value.bytes());
                }
            };
        }

        @Override
        public Converter<String, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
            return new Converter<String, RequestBody>() {
                @Override
                public RequestBody convert(String value) throws IOException {
                    return RequestBody.create(MediaType.parse("text/plain"), value);
                }
            };
        }
    }
}
