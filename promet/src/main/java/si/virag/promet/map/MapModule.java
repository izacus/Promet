package si.virag.promet.map;

import dagger.Module;
import dagger.Provides;

@Module
public class MapModule {

    static PrometMaps maps = new PrometMaps();

    public MapModule() {};

    @Provides
    PrometMaps getPrometMaps() {
        return maps;
    }

}
