package si.virag.promet.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;

import javax.inject.Inject;

import si.virag.promet.PrometApplication;
import si.virag.promet.utils.PrometSettings;

public class SetupPushRegistrationWorker extends Worker {

    private static final String TAG = "SetupPushRegistration";
    private static final String PREF_GCM_APP_VERSION = "GCM.Registered.Version";
    private static final String PUSH_TOPIC_NAME = "allRoadEvents";

    public static final String PREF_SHOULD_UPDATE_GCM_REGISTRATION = "GCM.ShouldRegister";

    @Inject PrometSettings prometSettings;

    public static void scheduleGcmUpdate(Context context) {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(SetupPushRegistrationWorker.class));
    }

    public SetupPushRegistrationWorker(@NonNull Context context,
                                       @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        PrometApplication app = (PrometApplication)context.getApplicationContext();
        app.component().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getInt(PREF_GCM_APP_VERSION, Integer.MIN_VALUE) == getAppVersion(getApplicationContext()) &&
                !prefs.getBoolean(PREF_SHOULD_UPDATE_GCM_REGISTRATION, false)) {
            Log.i(TAG, "No GCM update needed.");
            return Result.success();
        }

        if (prometSettings.getShouldReceiveNotifications()) {
            subscribe(context);
        } else {
            unsubscribe(context);
        }

        storeAppVersion(context);
        return Result.success();
    }

    private void subscribe(Context context) {
        Log.i(TAG, "Subscribing to topic.");
        FirebaseMessaging.getInstance().subscribeToTopic(PUSH_TOPIC_NAME).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Subscription successful, migrating stale data...");
                WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(RemoveGcmIdAfterTopicSubscriptionWorker.class));
            }
        });
    }

    private void unsubscribe(Context context) {
        Log.i(TAG, "Unsubscribing from topic.");
        FirebaseMessaging.getInstance().subscribeToTopic(PUSH_TOPIC_NAME).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Unsubscription successful, migrating stale data...");
                WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(RemoveGcmIdAfterTopicSubscriptionWorker.class));
            }
        });
    }

    private void storeAppVersion(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putInt(PREF_GCM_APP_VERSION, getAppVersion(context)).putBoolean(PREF_SHOULD_UPDATE_GCM_REGISTRATION, false).apply();
    }

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
