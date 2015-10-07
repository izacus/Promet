package si.virag.promet.gcm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import javax.inject.Inject;

import retrofit.RetrofitError;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.push.PushApi;
import si.virag.promet.utils.PrometSettings;

public class RegistrationService extends IntentService {

    private static final int INITIAL_RETRY_DELAY_SEC = 60;
    private static final int MAX_RETRY_DELAY_SEC = 3600;

    private static final String LOG_TAG = "Promet.GCM";
    private static final String GCM_ID = "857177207353";
    private static final String PARAM_CURRENT_DELAY = "Start.Delay";

    public static final String PREF_SHOULD_UPDATE_GCM_REGISTRATION = "GCM.ShouldRegister";
    public static final String PREF_GCM_APP_VERSION = "GCM.Registered.Version";
    public static final String PREF_GCM_KEY = "GCM.Key";

    @Inject PushApi pushApi;
    @Inject PrometSettings settings;

    public static void scheduleGcmUpdate(Context context) {
        Intent updateGcm = new Intent(context, RegistrationService.class);
        context.startService(updateGcm);
    }

    public static void scheduleGcmUpdate(Context context, int delay) {
        Log.d(LOG_TAG, "Rescheduling with delay of " + delay);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent serviceIntent = new Intent(context, RegistrationService.class);
            serviceIntent.putExtra(PARAM_CURRENT_DELAY, delay);
            PendingIntent pi = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (delay * 1000), pi);
        } else {
            PersistableBundle extras = new PersistableBundle();
            extras.putInt(PARAM_CURRENT_DELAY, delay);
            JobInfo jobInfo = new JobInfo.Builder(0, new ComponentName(context, RegistrationService.class))
                                  .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                                  .setExtras(extras)
                                  .setMinimumLatency(delay * 1000)
                                  .build();
            JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.schedule(jobInfo);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        PrometApplication app = (PrometApplication) getApplication();
        app.component().inject(this);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RegistrationService() {
        super("GCM Registration Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Starting GCM registration check...");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        InstanceID instanceID = InstanceID.getInstance(this);

        try {
            String gcmToken = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            String currentGcmId = getCurrentRegistrationId(prefs);
            if (!currentGcmId.equals(gcmToken)) {
                prefs.edit().putBoolean(PREF_SHOULD_UPDATE_GCM_REGISTRATION, true).apply();
            }

            if (prefs.getBoolean(PREF_SHOULD_UPDATE_GCM_REGISTRATION, true)) {
                registerGCMOnServer(prefs, gcmToken);
            }

        } catch (IOException e) {
            Log.d(LOG_TAG, "Failed to retrieve GCM ID.");

            int newDelay = intent.getIntExtra(PARAM_CURRENT_DELAY, INITIAL_RETRY_DELAY_SEC) * 2;
            if (newDelay < MAX_RETRY_DELAY_SEC) {
                scheduleGcmUpdate(getApplicationContext(), newDelay);
            } else {
                Log.w(LOG_TAG, "GCM registration delay limit reached, giving up.");
            }
        } finally {
            PushBroadcastReceiver.completeWakefulIntent(intent);
        }
    }


    private void registerGCMOnServer(SharedPreferences prefs, String gcmId) throws IOException{
        try
        {
            if (settings.getShouldReceiveNotifications()) {
                pushApi.register(gcmId);
            } else {
                pushApi.unregister(gcmId);
            }
        }
        catch (RetrofitError e) {
            Log.d(LOG_TAG, "Failed to push API key to server: " + e);
            throw new IOException("Failed to push API key to server.", e);
        }

        prefs.edit()
            .putBoolean(PREF_SHOULD_UPDATE_GCM_REGISTRATION, false)
            .putString(PREF_GCM_KEY, gcmId)
            .putInt(PREF_GCM_APP_VERSION, getAppVersion(getApplicationContext()))
            .apply();
        Log.i(LOG_TAG, "GCM ID registered ok.");
    }

    private String getCurrentRegistrationId(SharedPreferences prefs) {
        if (prefs.getInt(PREF_GCM_APP_VERSION, Integer.MIN_VALUE) != getAppVersion(getApplicationContext())) {
            Log.i(LOG_TAG, "Application has been updated, need to reregister GCM.");
            return "";
        }

        return prefs.getString(PREF_GCM_KEY, "");
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}
