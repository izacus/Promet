package si.virag.promet.api.opendata;

import retrofit.http.GET;
import rx.Observable;
import si.virag.promet.api.model.PrometEvents;


public interface OpenDataApi {

    @GET("/promet/events/")
    public Observable<PrometEvents> getEvents();

}
