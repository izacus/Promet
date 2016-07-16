package si.virag.promet;

import javax.inject.Singleton;

import dagger.Component;
import si.virag.promet.api.opendata.OpenDataApiModule;
import si.virag.promet.api.push.PushDataPrometApi;
import si.virag.promet.fragments.CamerasFragment;
import si.virag.promet.fragments.EventListFragment;
import si.virag.promet.fragments.MapFragment;
import si.virag.promet.gcm.NotificationStorageModule;
import si.virag.promet.gcm.PushIntentService;
import si.virag.promet.gcm.RegistrationService;
import si.virag.promet.map.LocationModule;
import si.virag.promet.map.MapModule;
import si.virag.promet.preferences.PrometPreferencesFragment;

@Singleton
@Component(modules = { OpenDataApiModule.class, MapModule.class, PrometApplicationModule.class, PushDataPrometApi.class, NotificationStorageModule.class, LocationModule.class })
public interface PrometComponent {

    public void inject(MainActivity mainActivity);
    public void inject(CameraDetailActivity cameraDetailActivity);
    public void inject(EventListFragment eventListFragment);
    public void inject(MapFragment mapFragment);
    public void inject(RegistrationService registrationService);
    public void inject(PushIntentService pushIntentService);
    public void inject(PrometPreferencesFragment prometPreferencesFragment);
    public void inject(CamerasFragment camerasFragment);
}
