package si.virag.promet.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import si.virag.promet.api.model.RoadType;
import si.virag.promet.gcm.RegistrationService;

public class PrometSettings {

    private static final String PREF_AVTOCESTE = "show.avtoceste";
    private static final String PREF_BORDER_CROSSINGS = "show.prehodi";
    private static final String PREF_REGIONALNE_CESTE = "show.regionalne.ceste";
    private static final String PREF_LOKALNE_CESTE = "show.lokalne.ceste";

    public static final String PREF_NOTIFICATIONS = "gcm_enabled";
    public static final String PREF_NOTIFICATIONS_CROSSINGS = "gcm_crossings";
    public static final String PREF_NOTIFICATIONS_HIGHWAYS = "gcm_highways";
    public static final String PREF_NOTIFICATIONS_REGIONAL = "gcm_regional";
    public static final String PREF_NOTIFICATIONS_LOCAL = "gcm_local";


    private final SharedPreferences preferences;
    private final Context context;

    private boolean showAvtoceste;
    private boolean showLokalneCeste;
    private boolean showRegionalneCeste;
    private boolean showBorderCrossings;
    private boolean receiveGcmNotifications;

    public PrometSettings(Context context) {
        this.context = context.getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        showAvtoceste = preferences.getBoolean(PREF_AVTOCESTE, true);
        showBorderCrossings = preferences.getBoolean(PREF_BORDER_CROSSINGS, true);
        showRegionalneCeste = preferences.getBoolean(PREF_REGIONALNE_CESTE, true);
        showLokalneCeste = preferences.getBoolean(PREF_LOKALNE_CESTE, true);
        receiveGcmNotifications = preferences.getBoolean(PREF_NOTIFICATIONS, true);
    }

    public boolean getShowAvtoceste() {
        return showAvtoceste;
    }

    public void setShowAvtoceste(boolean showAvtoceste) {
        this.showAvtoceste = showAvtoceste;
        preferences.edit().putBoolean(PREF_AVTOCESTE, showAvtoceste).apply();
    }

    public boolean getShowLokalneCeste() {
        return showLokalneCeste;
    }

    public void setShowLokalneCeste(boolean showLokalneCeste) {
        this.showLokalneCeste = showLokalneCeste;
        preferences.edit().putBoolean(PREF_LOKALNE_CESTE, showLokalneCeste).apply();

    }

    public boolean getShowRegionalneCeste() {
        return showRegionalneCeste;
    }

    public void setShowRegionalneCeste(boolean showRegionalneCeste) {
        this.showRegionalneCeste = showRegionalneCeste;
        preferences.edit().putBoolean(PREF_REGIONALNE_CESTE, showRegionalneCeste).apply();

    }

    public boolean getShowBorderCrossings() {
        return showBorderCrossings;
    }

    public void setShowBorderCrossings(boolean showBorderCrossings) {
        this.showBorderCrossings = showBorderCrossings;
        preferences.edit().putBoolean(PREF_BORDER_CROSSINGS, showBorderCrossings).apply();
    }

    public boolean getShouldReceiveNotifications() {
        return preferences.getBoolean(PREF_NOTIFICATIONS, false);
    }

    public boolean shouldShowNotification(RoadType type) {
        switch (type) {
            case MEJNI_PREHOD:
                return preferences.getBoolean(PREF_NOTIFICATIONS_CROSSINGS, true);
            case AVTOCESTA:
            case HITRA_CESTA:
                return preferences.getBoolean(PREF_NOTIFICATIONS_HIGHWAYS, true);
            case REGIONALNA_CESTA:
                return preferences.getBoolean(PREF_NOTIFICATIONS_REGIONAL, true);
            case LOKALNA_CESTA:
                return preferences.getBoolean(PREF_NOTIFICATIONS_LOCAL, true);
        }

        return true;
    }
}
