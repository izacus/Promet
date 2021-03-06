package si.virag.promet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.multidex.MultiDex;

import com.crashlytics.android.core.CrashlyticsCore;
import com.franmontiel.localechanger.LocaleChanger;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.Arrays;
import java.util.Locale;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import si.virag.promet.api.push.PushDataPrometApi;
import si.virag.promet.gcm.PushIntentService;
import si.virag.promet.gcm.SetupPushRegistrationWorker;

public class PrometApplication extends DaggerApplication {

    private PrometComponent component;

    @Override
    public void onCreate() {
        if (!Fabric.isInitialized()) {
            CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                    .disabled(BuildConfig.DEBUG)
                    .build();
            Fabric.with(this, crashlyticsCore);
        }
        super.onCreate();

        LocaleChanger.initialize(this, Arrays.asList(new Locale("en"), new Locale("sl")));
        AndroidThreeTen.init(this);
        Realm.init(this);
        createNotificationChannel();
        SetupPushRegistrationWorker.scheduleGcmUpdate(this);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        component = (PrometComponent) DaggerPrometComponent.builder()
                .applicationContext(this)
                .pushDataPrometApi(new PushDataPrometApi(this))
                .create(this);
        return component;
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
        MultiDex.install(this);
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
