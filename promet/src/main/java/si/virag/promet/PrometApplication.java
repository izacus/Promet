package si.virag.promet;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jakewharton.threetenabp.AndroidThreeTen;


import java.util.Locale;

import io.realm.Realm;
import si.virag.promet.api.data.PrometApiModule;
import si.virag.promet.api.push.PushDataPrometApi;
import si.virag.promet.gcm.RegistrationService;
import si.virag.promet.map.LocationModule;

public class PrometApplication extends Application {
    private static final String LOG_TAG = "Promet";

    public static Locale locale = null;
    private PrometComponent component;

    @Override
    public void onCreate() {
        checkUpdateLocale(this);
        super.onCreate();
        AndroidThreeTen.init(this);
        Realm.init(this);

        component = DaggerPrometComponent.builder()
                                          .prometApplicationModule(new PrometApplicationModule(this))
                                          .locationModule(new LocationModule(this))
                                          .prometApiModule(new PrometApiModule(this))
                                          .pushDataPrometApi(new PushDataPrometApi(this))
                                          .build();
        RegistrationService.scheduleGcmUpdate(this);
    }

    public PrometComponent component() {
        return component;
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
