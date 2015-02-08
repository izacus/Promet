package si.virag.promet.api.push;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.POST;

public interface PushApi {

    @POST("/register")
    public Response register(@Body String gcmId);

    @POST("/unregister")
    public Response unregister(@Body String gcmId);

}
