package si.virag.promet;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.franmontiel.localechanger.LocaleChanger;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.Arrays;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import si.virag.promet.api.data.PrometApiModule;
import si.virag.promet.api.push.PushDataPrometApi;
import si.virag.promet.gcm.PushIntentService;
import si.virag.promet.gcm.RegistrationService;
import si.virag.promet.map.LocationModule;

public class PrometApplication extends Application {

    private PrometComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        LocaleChanger.initialize(this, Arrays.asList(new Locale("en"), new Locale("sl")));

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        AndroidThreeTen.init(this);
        Realm.init(this);
        createNotificationChannel();
        component = DaggerPrometComponent.builder()
                .prometApplicationModule(new PrometApplicationModule(this))
                .locationModule(new LocationModule(this))
                .prometApiModule(new PrometApiModule(this))
                .pushDataPrometApi(new PushDataPrometApi(this))
                .build();
        RegistrationService.scheduleGcmUpdate(this);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(PushIntentService.DEFAULT_CHANNEL_ID, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.notification_channel_description));
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleChanger.onConfigurationChanged();
    }

    public PrometComponent component() {
        return component;
    }

}
