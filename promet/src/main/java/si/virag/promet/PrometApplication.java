package si.virag.promet;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import dagger.ObjectGraph;
import si.virag.promet.api.opendata.OpenDataApiModule;
import si.virag.promet.map.MapModule;

import java.util.Locale;

public class PrometApplication extends Application {

    public static final Locale locale = new Locale("sl-SI");

    private ObjectGraph graph;

    @Override
    public void onCreate() {
        setupLocale(this);
        super.onCreate();

        graph = ObjectGraph.create(
                OpenDataApiModule.class,
                MapModule.class,
                new PrometApplicationModule(this)
        );
    }

    public void inject(Object object) {
        graph.inject(object);
    }

    private void setupLocale(Context ctx) {
        Configuration c = ctx.getResources().getConfiguration();
        if (!c.locale.equals(locale)) {
            c.locale = locale;
            ctx.getResources().updateConfiguration(c, null);
        }
    }

}
