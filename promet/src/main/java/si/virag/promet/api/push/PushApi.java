package si.virag.promet.api.push;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface PushApi {

    @POST("/register")
    Response register(@Body String gcmId);

    @POST("/unregister")
    Response unregister(@Body String gcmId);

}
