package si.virag.promet.api.data;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.threeten.bp.ZonedDateTime;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Single;
import rx.functions.Action1;
import si.virag.promet.api.model.EventGroup;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.TrafficInfo;
import si.virag.promet.api.model.TrafficStatus;
import si.virag.promet.api.opendata.EventGroupAdapter;
import si.virag.promet.api.opendata.TrafficStatusAdapter;
import si.virag.promet.fragments.ui.EventListSorter;
import si.virag.promet.utils.DataUtils;

@Singleton
public class PrometApi {

    private final PrometDataApi prometApi;

    private Single<TrafficInfo> trafficInfoObserver;

    @Inject
    public PrometApi(@NonNull Context context) {
        final String userAgent = DataUtils.getUserAgent(context);
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        okHttpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .header("User-Agent", userAgent)
                        .method(chain.request().method(), chain.request().body())
                        .build();

                return chain.proceed(request);
            }
        });

        // Server supports only TLS 1.2 which is enabled on newer devices.
        String protocol = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP ? "https" : "http";
        String endpointUrl = protocol + "://prometapi.virag.si";

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(EventGroup.class, new EventGroupAdapter())
                .registerTypeAdapter(TrafficStatus.class, new TrafficStatusAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeConverter())
                .create();


        Retrofit adapter = new Retrofit.Builder()
                                .baseUrl(endpointUrl)
                                .client(okHttpClient.build())
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .addCallAdapterFactory(RxJavaCallAdapterFactory.createAsync())
                                .build();

        prometApi = adapter.create(PrometDataApi.class);
        createTrafficInfoObserver();
    }

    private void createTrafficInfoObserver() {
        trafficInfoObserver = prometApi.getData()
                .doOnNext(new Action1<TrafficInfo>() {
                    @Override
                    public void call(TrafficInfo trafficInfo) {
                        for (PrometEvent event : trafficInfo.events) {
                            if (event.isBorderCrossing || (event.eventGroup == null && event.descriptionSl != null)) {
                                event.eventGroup = DataUtils.roadPriorityToRoadType(event.roadPriority, event.isBorderCrossing);
                            }
                        }

                        Collections.sort(trafficInfo.events, new EventListSorter());
                    }
                })
                .cache()
                .toSingle();
    }

    public Single<TrafficInfo> getTrafficInfo() {
        return trafficInfoObserver;
    }

    public Single<TrafficInfo> getReloadTrafficInfo() {
        createTrafficInfoObserver();
        return trafficInfoObserver;
    }
}
