package si.virag.promet.api.opendata;

import dagger.Module;
import dagger.Provides;
import si.virag.promet.api.PrometApi;
import si.virag.promet.fragments.EventListFragment;
import si.virag.promet.fragments.MapFragment;

import javax.inject.Singleton;

@Module(
        injects = { MapFragment.class, EventListFragment.class },
        complete = false
)
public class OpenDataApiModule {
    static OpenDataPrometApi prometApi = new OpenDataPrometApi();

    public OpenDataApiModule() {};

    @Provides @Singleton PrometApi getPrometApi() {
        return prometApi;
    }
}
