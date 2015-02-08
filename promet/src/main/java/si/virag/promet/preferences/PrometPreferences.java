package si.virag.promet.preferences;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;

import javax.inject.Inject;

import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.gcm.RegistrationService;
import si.virag.promet.utils.PrometSettings;

public class PrometPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ListPreference langPreference;

    @Inject PrometSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getActionBar();
        if (ab != null)
            ab.setTitle(R.string.app_name); // This is wrong if language was changed, so force change :)

        addPreferencesFromResource(R.xml.preferences);

        langPreference = (ListPreference) findPreference("app_language");
        langPreference.setSummary(langPreference.getEntry());

        PrometApplication application = (PrometApplication) getApplication();
        application.component().inject(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("app_language")) {
            langPreference.setSummary(langPreference.getEntry());
            ((PrometApplication)getApplication()).checkUpdateLocale(this);
            finish();
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else if (key.equalsIgnoreCase("gcm_enabled")) {
            checkSetEnabledNotificationPreferences();
            sharedPreferences.edit().putBoolean(RegistrationService.PREF_SHOULD_UPDATE_GCM_REGISTRATION, true).apply();
        }

        settings.reload();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSetEnabledNotificationPreferences();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        Intent i = new Intent(this, RegistrationService.class);
        startService(i);
    }

    private void checkSetEnabledNotificationPreferences() {
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        boolean enable = preferences.getBoolean(PrometSettings.PREF_NOTIFICATIONS, false);

        findPreference(PrometSettings.PREF_NOTIFICATIONS_CROSSINGS).setEnabled(enable);
        findPreference(PrometSettings.PREF_NOTIFICATIONS_HIGHWAYS).setEnabled(enable);
        findPreference(PrometSettings.PREF_NOTIFICATIONS_REGIONAL).setEnabled(enable);
        findPreference(PrometSettings.PREF_NOTIFICATIONS_LOCAL).setEnabled(enable);
    }
}
