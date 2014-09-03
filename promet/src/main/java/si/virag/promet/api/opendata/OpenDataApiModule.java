package si.virag.promet.api.opendata;

import dagger.Module;
import dagger.Provides;
import si.virag.promet.MainActivity;
import si.virag.promet.api.PrometApi;

@Module(
        injects = MainActivity.class,
        library = true
)
public class OpenDataApiModule {

    static OpenDataPrometApi prometApi = new OpenDataPrometApi();

    @Provides PrometApi getPrometApi() {
        return prometApi;
    }
}
