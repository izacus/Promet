package si.virag.promet;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;
import dagger.ObjectGraph;
import si.virag.promet.api.opendata.OpenDataApiModule;
import si.virag.promet.map.MapModule;

import java.util.Locale;

public class PrometApplication extends Application {
    private static final String LOG_TAG = "Promet";

    public static Locale locale = null;

    private ObjectGraph graph;

    @Override
    public void onCreate() {
        checkUpdateLocale(this);
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

    public void checkUpdateLocale(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String language = prefs.getString("app_language", "default");
        if (language.equalsIgnoreCase("default")) return;

        Configuration c = ctx.getResources().getConfiguration();
        if (!c.locale.equals(locale) || !locale.getLanguage().equalsIgnoreCase(language)) {
            locale = new Locale(language);
            Log.d(LOG_TAG, "Switching language to " + locale.getLanguage() + "-" + locale.getCountry());
            c.locale = locale;
            Locale.setDefault(locale);
            getBaseContext().getResources().updateConfiguration(c, getBaseContext().getResources().getDisplayMetrics());
        }
    }

}
