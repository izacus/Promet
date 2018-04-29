package si.virag.promet.preferences;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.franmontiel.localechanger.LocaleChanger;

import java.util.Locale;

import javax.inject.Inject;

import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.gcm.RegisterFcmTokenJob;
import si.virag.promet.utils.PrometSettings;

public class PrometPreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ListPreference langPreference;
    @Inject PrometSettings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PrometApplication application = (PrometApplication) getContext().getApplicationContext();
        application.component().inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_lollipop, rootKey);

        langPreference = (ListPreference) findPreference("app_language");
        langPreference.setSummary(langPreference.getEntry());
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();
        if (activity == null) return;

        if (key.equalsIgnoreCase("app_language")) {
            langPreference.setSummary(langPreference.getEntry());

            if (langPreference.getValue().equals("default")) {
                LocaleChanger.resetLocale();
            } else {
                LocaleChanger.setLocale(new Locale(langPreference.getValue()));
            }

            activity.finish();
            Intent i = new Intent(activity, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else if (key.equalsIgnoreCase("gcm_enabled")) {
            checkSetEnabledNotificationPreferences();
            sharedPreferences.edit().putBoolean(RegisterFcmTokenJob.PREF_SHOULD_UPDATE_GCM_REGISTRATION, true).apply();
        }

        settings.reload();
    }



    @Override
    public void onStart() {
        super.onStart();
        checkSetEnabledNotificationPreferences();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Activity activity = getActivity();
        if (activity == null) return;

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        RegisterFcmTokenJob.scheduleGcmUpdate();
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
