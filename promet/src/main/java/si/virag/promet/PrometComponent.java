package si.virag.promet;

import javax.inject.Singleton;

import dagger.Component;
import si.virag.promet.api.opendata.OpenDataApiModule;
import si.virag.promet.api.push.PushDataPrometApi;
import si.virag.promet.fragments.EventListFragment;
import si.virag.promet.fragments.MapFragment;
import si.virag.promet.gcm.RegistrationService;
import si.virag.promet.map.MapModule;

@Singleton
@Component(modules = { OpenDataApiModule.class, MapModule.class, PrometApplicationModule.class, PushDataPrometApi.class})
public interface PrometComponent {

    public void inject(MainActivity mainActivity);
    public void inject(EventListFragment eventListFragment);
    public void inject(MapFragment mapFragment);
    public void inject(RegistrationService registrationService);

}
