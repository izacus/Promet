package si.virag.promet.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrometSettings {

    private static final String PREF_AVTOCESTE = "show.avtoceste";
    private static final String PREF_REGIONALNE_CESTE = "show.regionalne.ceste";
    private static final String PREF_LOKALNE_CESTE = "show.lokalne.ceste";

    private final SharedPreferences preferences;

    private boolean showAvtoceste;
    private boolean showLokalneCeste;
    private boolean showRegionalneCeste;

    public PrometSettings(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        showAvtoceste = preferences.getBoolean(PREF_AVTOCESTE, true);
        showRegionalneCeste = preferences.getBoolean(PREF_REGIONALNE_CESTE, true);
        showLokalneCeste = preferences.getBoolean(PREF_LOKALNE_CESTE, true);
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
}
