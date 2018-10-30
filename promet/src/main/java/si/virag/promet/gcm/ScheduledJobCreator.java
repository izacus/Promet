package si.virag.promet.gcm;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ScheduledJobCreator implements JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case RegisterFcmTokenJob.TAG:
                return new RegisterFcmTokenJob();
            case ClearNotificationsJob.TAG:
                return new ClearNotificationsJob();
            default:
                return null;
        }
    }
}
