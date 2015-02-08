package si.virag.promet;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import si.virag.promet.utils.PrometSettings;

@Module
public class PrometApplicationModule {

    private final Context ctx;

    public PrometApplicationModule(Context context) {
        this.ctx = context;
    }

    @Provides @Singleton PrometSettings provideSettings() {
        return new PrometSettings(ctx);
    }
}
