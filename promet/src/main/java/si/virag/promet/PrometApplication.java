package si.virag.promet;

import android.app.Application;
import dagger.ObjectGraph;
import si.virag.promet.api.opendata.OpenDataApiModule;

public class PrometApplication extends Application {

    private ObjectGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();

        graph = ObjectGraph.create(
                OpenDataApiModule.class
        );
    }

    public void inject(Object object) {
        graph.inject(object);
    }
}
