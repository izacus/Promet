package si.virag.promet;

import android.content.Context;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import si.virag.promet.api.push.PushDataPrometApi;
import si.virag.promet.gcm.RegisterFcmTokenJob;
import si.virag.promet.map.MapModule;

@Singleton
@Component(modules = {AndroidSupportInjectionModule.class, MapModule.class,PushDataPrometApi.class, PrometUiModule.class})
public interface PrometComponent extends AndroidInjector<PrometApplication> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<PrometApplication> {
        @BindsInstance
        abstract Builder applicationContext(Context applicationContext);

        abstract Builder pushDataPrometApi(PushDataPrometApi api);

        public abstract PrometComponent build();
    }

    void inject(RegisterFcmTokenJob job);
}
