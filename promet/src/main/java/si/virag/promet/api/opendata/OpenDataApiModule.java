package si.virag.promet.api.opendata;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import si.virag.promet.api.PrometApi;

@Module
public class OpenDataApiModule {
    static OpenDataPrometApi prometApi;

    public OpenDataApiModule(Context context) {
        prometApi = new OpenDataPrometApi(context);
    }

    @Provides @Singleton PrometApi getPrometApi() {
        return prometApi;
    }
}
