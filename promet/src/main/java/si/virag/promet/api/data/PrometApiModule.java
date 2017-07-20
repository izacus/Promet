package si.virag.promet.api.data;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PrometApiModule {

    private final Context context;

    public PrometApiModule(@NonNull Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    public PrometApi provideApi() {
        return new PrometApi(context);
    }

}
