package si.virag.promet.gcm;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class PrometInstanceIDListenerService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        RegisterFcmTokenJob.scheduleGcmUpdate();
    }
}
