package si.virag.promet.api.data;

import retrofit2.http.GET;
import rx.Observable;
import si.virag.promet.api.model.TrafficInfo;

public interface PrometDataApi {

    @GET("/data")
    Observable<TrafficInfo> getData();

}
