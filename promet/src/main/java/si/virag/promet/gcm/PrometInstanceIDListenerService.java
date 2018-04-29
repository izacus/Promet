package si.virag.promet.gcm;

import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class PrometInstanceIDListenerService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        Intent registrationIntent = new Intent(this, RegistrationService.class);
        startService(registrationIntent);
    }
}
