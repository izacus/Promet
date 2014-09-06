package si.virag.promet.api.opendata;


import android.util.Log;
import com.google.common.collect.ImmutableList;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.PrometEvents;
import si.virag.promet.api.model.RoadType;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class OpenDataPrometApi extends PrometApi {

    private static final String LOG_TAG = "Promet.OpenDataPrometApi";
    private final OpenDataApi openDataApi;

    public OpenDataPrometApi() {

        Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(RoadType.class, new RoadTypeAdapter())
                    .registerTypeAdapter(Date.class, new EpochDateTypeAdapter())
                    .create();

        RestAdapter adapter = new RestAdapter.Builder()
                                             .setEndpoint("http://www.opendata.si")
                                             .setConverter(new GsonConverter(gson))
                                             .setLogLevel(RestAdapter.LogLevel.FULL)
                                             .build();


        openDataApi = adapter.create(OpenDataApi.class);
    }

    @Override
    public Observable<ImmutableList<PrometEvent>> getPrometEvents() {
        return openDataApi.getEvents()
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
                                  if (prometEvent.roadType == null && prometEvent.description != null) {
                                    attemptAssignRoadType(prometEvent);
                                  }

                                  return prometEvent;
                              }
                          })
                          .toSortedList(new Func2<PrometEvent, PrometEvent, Integer>() {
                              @Override
                              public Integer call(PrometEvent lhs, PrometEvent rhs) {
                                   if (lhs.roadType == null)
                                      return rhs.roadType == null ? 0 : 1;

                                  if (rhs.roadType == null)
                                      return -1;

                                  return lhs.roadType.compareTo(rhs.roadType);
                              }
                          })
                          .map(new Func1<List<PrometEvent>, ImmutableList<PrometEvent>>() {
                              @Override
                              public ImmutableList<PrometEvent> call(List<PrometEvent> prometEvents) {
                                  return ImmutableList.copyOf(prometEvents);
                              }
                          });
    }

    private void attemptAssignRoadType(PrometEvent prometEvent) {
        String letters = prometEvent.description.substring(0, 2);
        prometEvent.roadType = RoadTypeAdapter.typeMapping.get(letters);

        if (prometEvent.roadType == null) {
            // See if we can use some heuroistics
            String description = prometEvent.description.toLowerCase();
            if (description.startsWith("na avtocesti"))
                prometEvent.roadType = RoadType.AVTOCESTA;
            else if (description.contains("obvoznici"))
                prometEvent.roadType = RoadType.HITRA_CESTA;
        }

        if (prometEvent.roadType != null)
            Log.d(LOG_TAG, "Assigned " + prometEvent.roadType + " for " + prometEvent.description);
    }
}
