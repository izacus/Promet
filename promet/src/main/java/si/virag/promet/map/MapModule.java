package si.virag.promet.map;

import dagger.Module;
import dagger.Provides;
import si.virag.promet.fragments.MapFragment;

@Module(
        injects = MapFragment.class,
        complete = false
)
public class MapModule {

    static PrometMaps maps = new PrometMaps();

    public MapModule() {};

    @Provides
    PrometMaps getPrometMaps() {
        return maps;
    }

}
