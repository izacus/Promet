package si.virag.promet;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import si.virag.promet.fragments.CamerasFragment;
import si.virag.promet.fragments.EventListFragment;
import si.virag.promet.fragments.MapFragment;
import si.virag.promet.gcm.PushIntentService;
import si.virag.promet.preferences.PrometPreferencesFragment;

@Module
abstract class PrometUiModule {

    @ContributesAndroidInjector
    abstract MainActivity contributesMainActivityInjector();

    @ContributesAndroidInjector
    abstract CameraDetailActivity contributesDetailActivityInjector();

    @ContributesAndroidInjector
    abstract MapFragment contributesMapFragmentInjector();

    @ContributesAndroidInjector
    abstract EventListFragment contributesEventListFragmentInjector();

    @ContributesAndroidInjector
    abstract CamerasFragment contributesCamerasInjector();

    @ContributesAndroidInjector
    abstract PrometPreferencesFragment contributesPreferencesFragment();

    @ContributesAndroidInjector
    abstract PushIntentService contributesPushServiceInjector();
}
