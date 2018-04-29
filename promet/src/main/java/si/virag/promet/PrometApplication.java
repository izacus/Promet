package si.virag.promet;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.crashlytics.android.core.CrashlyticsCore;
import com.evernote.android.job.JobManager;
import com.franmontiel.localechanger.LocaleChanger;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.Arrays;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import si.virag.promet.api.data.PrometApiModule;
import si.virag.promet.api.push.PushDataPrometApi;
import si.virag.promet.gcm.PushIntentService;
import si.virag.promet.gcm.RegisterFcmTokenJob;
import si.virag.promet.gcm.ScheduledJobCreator;
import si.virag.promet.map.LocationModule;

public class PrometApplication extends Application {

    private PrometComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        LocaleChanger.initialize(this, Arrays.asList(new Locale("en"), new Locale("sl")));
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, crashlyticsCore);

        AndroidThreeTen.init(this);
        Realm.init(this);
        createNotificationChannel();
        component = DaggerPrometComponent.builder()
                .prometApplicationModule(new PrometApplicationModule(this))
                .locationModule(new LocationModule(this))
                .prometApiModule(new PrometApiModule(this))
                .pushDataPrometApi(new PushDataPrometApi(this))
                .build();

        JobManager.create(this).addJobCreator(new ScheduledJobCreator());
        RegisterFcmTokenJob.scheduleGcmUpdate();
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (BuildConfig.DEBUG) {
            MultiDex.install(this);
        }
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
