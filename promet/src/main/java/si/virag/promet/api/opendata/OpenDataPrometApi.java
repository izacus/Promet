package si.virag.promet.api.opendata;


import com.google.common.collect.ImmutableList;
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
                                             .setEndpoint("http://www.opendata.si")
                                             .build();


        openDataApi = adapter.create(OpenDataApi.class);
    }

    @Override
    public Observable<ImmutableList<PrometEvent>> getPrometEvents() {
        return openDataApi.getEvents()
                          .map(new Func1<PrometEvents, ImmutableList<PrometEvent>>() {
            @Override
            public ImmutableList<PrometEvent> call(PrometEvents prometEvents) {
                return ImmutableList.copyOf(prometEvents.events.events);
            }
        });
    }
}
