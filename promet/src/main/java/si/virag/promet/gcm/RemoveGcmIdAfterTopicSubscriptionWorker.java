package si.virag.promet.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Response;
import si.virag.promet.PrometApplication;
import si.virag.promet.api.push.PrometPushApi;

public class RemoveGcmIdAfterTopicSubscriptionWorker extends Worker {

    private static final String TAG = "RemoveGcm";
    private static final String PREF_GCM_KEY = "GCM.Key";

    @Inject PrometPushApi pushApi;

    public RemoveGcmIdAfterTopicSubscriptionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        PrometApplication app = (PrometApplication) getApplicationContext();
        app.component().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String gcmKey = prefs.getString(PREF_GCM_KEY, "");
        if (TextUtils.isEmpty(gcmKey)) {
            Log.d(TAG, "No migration needed.");
            removeKeys(prefs);
            return Result.success();
        }

        try {
            Response<String> response = pushApi.unregister(gcmKey).execute();
            if (response.isSuccessful()) {
                Log.d(TAG, "Key removal successful.");
                removeKeys(prefs);
                return Result.success();
            } else {
                Log.e(TAG, "Failed to remove key: " + response.errorBody());
                if (response.code() >= 500) {
                    return Result.failure();
                } else {
                    return Result.retry();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to remove key", e);
            return Result.retry();
        }
    }

    private void removeKeys(SharedPreferences prefs) {
        prefs.edit().remove(PREF_GCM_KEY).apply();
    }
}
