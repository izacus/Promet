package si.virag.promet.api.opendata;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.functions.Func1;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.PrometEvents;
import si.virag.promet.api.model.RoadType;
import si.virag.promet.fragments.ui.EventListSorter;
import si.virag.promet.utils.DataUtils;

public class OpenDataPrometApi extends PrometApi {

    private static final String LOG_TAG = "Promet.OpenDataApi";
    private final OpenDataApi openDataApi;

    private Observable<List<PrometEvent>> prometEventsObserver;

    public OpenDataPrometApi() {

        Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(RoadType.class, new RoadTypeAdapter())
                    .registerTypeAdapter(Date.class, new EpochDateTypeAdapter())
                    .create();

        RestAdapter adapter = new RestAdapter.Builder()
                                             .setEndpoint("http://www.opendata.si")
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
                        if (prometEvent.isBorderCrossing || (prometEvent.roadType == null && prometEvent.description != null)) {
                            prometEvent.roadType = DataUtils.roadPriorityToRoadType(prometEvent.roadPriority, prometEvent.isBorderCrossing);
                        }

                        return prometEvent;
                    }
                })
                .toSortedList(new EventListSorter())
                .cache();
    }
}
