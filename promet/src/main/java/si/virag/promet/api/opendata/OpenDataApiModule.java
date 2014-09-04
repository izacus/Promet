package si.virag.promet.api.opendata;

import dagger.Module;
import dagger.Provides;
import si.virag.promet.api.PrometApi;
import si.virag.promet.fragments.MapFragment;

@Module(
        injects = MapFragment.class,
        complete = false
)
public class OpenDataApiModule {
    static OpenDataPrometApi prometApi = new OpenDataPrometApi();

    public OpenDataApiModule() {};

    @Provides PrometApi getPrometApi() {
        return prometApi;
    }
}
