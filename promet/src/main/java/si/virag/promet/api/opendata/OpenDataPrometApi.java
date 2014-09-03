package si.virag.promet.api.opendata;


import retrofit.RestAdapter;
import rx.Observable;
import rx.functions.Func1;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.PrometEvents;

import java.util.List;

public class OpenDataPrometApi extends PrometApi {

    private final OpenDataApi openDataApi;

    public OpenDataPrometApi() {

        RestAdapter adapter = new RestAdapter.Builder()
                                             .setEndpoint("https://www.opendata.si")
                                             .build();


        openDataApi = adapter.create(OpenDataApi.class);
    }

    @Override
    public Observable<List<PrometEvent>> getPrometEvents() {
        return openDataApi.getEvents()
                          .cache()
                          .map(new Func1<PrometEvents, List<PrometEvent>>() {
            @Override
            public List<PrometEvent> call(PrometEvents prometEvents) {
                return prometEvents.events.events;
            }
        });
    }
}
