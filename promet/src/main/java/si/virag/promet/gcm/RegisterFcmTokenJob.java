package si.virag.promet.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.threeten.bp.Duration;

import java.io.IOException;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import rx.Single;
import si.virag.promet.PrometApplication;
import si.virag.promet.api.push.PrometPushApi;
import si.virag.promet.utils.PrometSettings;

public final class RegisterFcmTokenJob extends Job {

    private static final String LOG_TAG = "Promet.GCM";
    private static final String PREF_GCM_APP_VERSION = "GCM.Registered.Version";
    private static final String PREF_GCM_KEY = "GCM.Key";

    public static final String TAG = "register_fcm_job";
    public static final String PREF_SHOULD_UPDATE_GCM_REGISTRATION = "GCM.ShouldRegister";

    public static void scheduleGcmUpdate() {
        new JobRequest.Builder(TAG)
                .setExecutionWindow(1, Duration.ofMinutes(5).toMillis())
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .setUpdateCurrent(true)
                .setBackoffCriteria(15000, JobRequest.BackoffPolicy.EXPONENTIAL)
                .build()
                .scheduleAsync();
    }

    @Inject PrometPushApi pushApi;
    @Inject PrometSettings settings;

    private static Single<String> getInstanceId() {
        return Single.create(subscriber -> {
            Task<InstanceIdResult> instanceIdResult = FirebaseInstanceId.getInstance().getInstanceId();
            instanceIdResult.addOnSuccessListener(result -> subscriber.onSuccess(result.getToken()));
            instanceIdResult.addOnFailureListener(subscriber::onError);
            instanceIdResult.addOnCanceledListener(() -> subscriber.onError(new IOException("Task cancelled.")));
        });
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        if (pushApi == null || settings == null ) {
            PrometApplication app = (PrometApplication) getContext().getApplicationContext();
            app.component().inject(this);
        }

        Log.d(LOG_TAG, "Starting GCM registration check...");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        try {
            String gcmToken = getInstanceId().toBlocking().value();
            String currentGcmId = getCurrentRegistrationId(prefs);
            if (!currentGcmId.equals(gcmToken)) {
                prefs.edit().putBoolean(PREF_SHOULD_UPDATE_GCM_REGISTRATION, true).apply();
            }

            if (prefs.getBoolean(PREF_SHOULD_UPDATE_GCM_REGISTRATION, true)) {
                registerGCMOnServer(prefs, gcmToken);
            }

        } catch (IOException e) {
            Crashlytics.logException(e);
            Log.e(LOG_TAG, "Failed to retrieve GCM ID.", e);
            Crashlytics.logException(e);
            return Result.RESCHEDULE;
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "GCM services not available!", e);
            Crashlytics.logException(e);
            return Result.FAILURE;
        }

        return Result.SUCCESS;
    }

    private void registerGCMOnServer(SharedPreferences prefs, String gcmId) throws IOException {
        if (TextUtils.isEmpty(gcmId)) {
            throw new IOException("GCM ID not yet retrieved.");
        }

        Log.i(TAG, "GCM ID registering: " + gcmId);

        try
        {
            if (settings.getShouldReceiveNotifications()) {
                pushApi.register(gcmId).execute();
            } else {
                pushApi.unregister(gcmId).execute();
            }
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Failed to push API key to server", e);
            Crashlytics.logException(e);
            throw e;
        }

        prefs.edit()
                .putBoolean(PREF_SHOULD_UPDATE_GCM_REGISTRATION, false)
                .putString(PREF_GCM_KEY, gcmId)
                .putInt(PREF_GCM_APP_VERSION, getAppVersion(getContext()))
                .apply();
        Log.i(LOG_TAG, "GCM ID registered ok.");
    }

    private String getCurrentRegistrationId(SharedPreferences prefs) {
        if (prefs.getInt(PREF_GCM_APP_VERSION, Integer.MIN_VALUE) != getAppVersion(getContext())) {
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
