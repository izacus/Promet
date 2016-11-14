package si.virag.promet.api.opendata;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.functions.Func1;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.EventGroup;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.api.model.PrometCameras;
import si.virag.promet.api.model.PrometCounter;
import si.virag.promet.api.model.PrometCounters;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.PrometEvents;
import si.virag.promet.api.model.TrafficStatus;
import si.virag.promet.fragments.ui.CameraListSorter;
import si.virag.promet.fragments.ui.EventListSorter;
import si.virag.promet.utils.DataUtils;

public class OpenDataPrometApi extends PrometApi {

    private static final String LOG_TAG = "Promet.OpenDataApi";
    private final OpenDataApi openDataApi;

    private Observable<List<PrometEvent>> prometEventsObserver;
    private Observable<List<PrometCamera>> prometCameras;

    public OpenDataPrometApi(Context context) {

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
                    .registerTypeAdapter(ZonedDateTime.class, new EpochDateTypeAdapter())
                    .registerTypeAdapter(TrafficStatus.class, new TrafficStatusAdapter())
                    .registerTypeAdapter(Double.class, new FunnyDoubleAdapter())
                    .create();

        RestAdapter adapter = new RestAdapter.Builder()
                                             .setEndpoint("http://www.opendata.si")
                                             .setRequestInterceptor(ri)
                                             .setConverter(new GsonConverter(gson))
                                             .build();


        openDataApi = adapter.create(OpenDataApi.class);
        createPrometEventsObserver();
        createPrometCamerasObserver();
    }

    @Override
    public Observable<List<PrometEvent>> getReloadPrometEvents() {
        createPrometEventsObserver();
        return prometEventsObserver;
    }

    @Override
    public Observable<List<PrometEvent>> getPrometEvents() {
        return prometEventsObserver;
    }

    @Override
    public Observable<List<PrometCounter>> getPrometCounters() {
        return Observable.just((List<PrometCounter>)new ArrayList<PrometCounter>());
        /*return openDataApi.getCounters()
               .flatMap(new Func1<PrometCounters, Observable<List<PrometCounter>>>() {
                   @Override
                   public Observable<List<PrometCounter>> call(PrometCounters prometCounters) {
                       return Observable.just(prometCounters.counters.get(0).data.counters);
                   }
               });*/
    }

    @Override
    public Observable<List<PrometCamera>> getPrometCameras() {
        return prometCameras;
    }

    private void createPrometEventsObserver() {
        prometEventsObserver = openDataApi.getEvents()
                .flatMap(new Func1<PrometEvents, Observable<PrometEvent>>() {
                    @Override
                    public Observable<PrometEvent> call(PrometEvents prometEvents) {
                        return Observable.from(prometEvents.events.get(0).data.events);
                    }
                })
                .map(new Func1<PrometEvent, PrometEvent>() {
                    @Override
                    public PrometEvent call(PrometEvent prometEvent) {
                        // Take first two letters of description and see if we can extract the road type from there.
                        if (prometEvent.isBorderCrossing || (prometEvent.eventGroup == null && prometEvent.description != null)) {
                            prometEvent.eventGroup = DataUtils.roadPriorityToRoadType(prometEvent.roadPriority, prometEvent.isBorderCrossing);
                        }

                        return prometEvent;
                    }
                })
                .toSortedList(new EventListSorter())
                .cache();
    }

    private void createPrometCamerasObserver() {
        prometCameras = openDataApi.getCameras()
                                   .flatMap(new Func1<PrometCameras, Observable<PrometCamera>>() {
                                       @Override
                                       public Observable<PrometCamera> call(PrometCameras prometCameras) {
                                           return Observable.from(prometCameras.feed.get(0).data.cameras);
                                       }
                                   })
                                   .filter(new Func1<PrometCamera, Boolean>() {
                                       @Override
                                       public Boolean call(PrometCamera prometCamera) {
                                           return prometCamera.cameras != null && prometCamera.cameras.size() > 0;
                                       }
                                   })
                                   .toSortedList(new CameraListSorter())

                                   .cache();
    }
}
