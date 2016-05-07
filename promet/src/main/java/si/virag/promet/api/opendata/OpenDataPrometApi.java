package si.virag.promet.api.opendata;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.threeten.bp.LocalDateTime;

import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.functions.Func1;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.EventGroup;
import si.virag.promet.api.model.PrometCounter;
import si.virag.promet.api.model.PrometCounters;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.PrometEvents;
import si.virag.promet.api.model.TrafficStatus;
import si.virag.promet.fragments.ui.EventListSorter;
import si.virag.promet.utils.DataUtils;

public class OpenDataPrometApi extends PrometApi {

    private static final String LOG_TAG = "Promet.OpenDataApi";
    private final OpenDataApi openDataApi;

    private Observable<List<PrometEvent>> prometEventsObserver;

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
                    .registerTypeAdapter(LocalDateTime.class, new EpochDateTypeAdapter())
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
        return openDataApi.getCounters()
               .flatMap(new Func1<PrometCounters, Observable<List<PrometCounter>>>() {
                   @Override
                   public Observable<List<PrometCounter>> call(PrometCounters prometCounters) {
                       return Observable.just(prometCounters.counters.counters);
                   }
               });
    }

    private void createPrometEventsObserver() {
        prometEventsObserver = openDataApi.getEvents()
                .flatMap(new Func1<PrometEvents, Observable<PrometEvent>>() {
                    @Override
                    public Observable<PrometEvent> call(PrometEvents prometEvents) {
                        return Observable.from(prometEvents.events.events);
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
}
