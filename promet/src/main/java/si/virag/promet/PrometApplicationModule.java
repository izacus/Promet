package si.virag.promet;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import si.virag.promet.fragments.MapFragment;
import si.virag.promet.gcm.RegistrationService;
import si.virag.promet.utils.PrometSettings;

import javax.inject.Singleton;

@Module(
        injects = { MainActivity.class, MapFragment.class, PrometSettings.class },
        complete = false
)
public class PrometApplicationModule {

    private final Context ctx;

    public PrometApplicationModule(Context context) {
        this.ctx = context;
    }

    @Provides @Singleton PrometSettings provideSettings() {
        return new PrometSettings(ctx);
    }
}
