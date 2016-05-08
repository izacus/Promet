package si.virag.promet.api.opendata;

import retrofit.http.GET;
import rx.Observable;
import si.virag.promet.api.model.PrometCameras;
import si.virag.promet.api.model.PrometCounters;
import si.virag.promet.api.model.PrometEvents;


public interface OpenDataApi {

    @GET("/promet/events/")
    Observable<PrometEvents> getEvents();

    @GET("/promet/counters/")
    Observable<PrometCounters> getCounters();

    @GET("/promet/cameras/")
    Observable<PrometCameras> getCameras();
}
