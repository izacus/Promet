package si.virag.promet.api.push;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PrometPushApi {

    @POST("/register")
    Call<String> register(@Body String gcmId);

    @POST("/unregister")
    Call<String> unregister(@Body String gcmId);

}
