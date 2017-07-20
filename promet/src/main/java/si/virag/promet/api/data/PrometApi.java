package si.virag.promet.api.data;


import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.threeten.bp.ZonedDateTime;

import java.util.Collections;

import dagger.Module;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Single;
import rx.functions.Action1;
import si.virag.promet.api.data.PrometDataApi;
import si.virag.promet.api.model.EventGroup;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.TrafficInfo;
import si.virag.promet.api.model.TrafficStatus;
import si.virag.promet.api.opendata.EventGroupAdapter;
import si.virag.promet.api.opendata.TrafficStatusAdapter;
import si.virag.promet.fragments.ui.EventListSorter;
import si.virag.promet.utils.DataUtils;

public class PrometApi {

    private final PrometDataApi prometApi;

    private Single<TrafficInfo> trafficInfoObserver;

    public PrometApi(@NonNull Context context) {
        final String userAgent = DataUtils.getUserAgent(context);
        RequestInterceptor ri = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("User-Agent", userAgent);
            }
        };

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(EventGroup.class, new EventGroupAdapter())
                .registerTypeAdapter(TrafficStatus.class, new TrafficStatusAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeConverter())
                .create();

        RestAdapter adapter = new RestAdapter.Builder()
                                .setEndpoint("http://prometapi.virag.si")
                                .setRequestInterceptor(ri)
                                .setConverter(new GsonConverter(gson))
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
