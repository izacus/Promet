package si.virag.promet.api.opendata;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import si.virag.promet.api.PrometApi;

@Module
public class OpenDataApiModule {
    static OpenDataPrometApi prometApi = new OpenDataPrometApi();

    public OpenDataApiModule() {};

    @Provides @Singleton PrometApi getPrometApi() {
        return prometApi;
    }
}
