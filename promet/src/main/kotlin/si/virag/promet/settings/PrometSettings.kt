package si.virag.promet.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Users settings for this app
 */
class PrometSettings(val context: Context) {

    interface OnSettingsChangedListener {
        fun onSettingsChanged()
    }

    val PREF_AVTOCESTE = "show.avtoceste";
    val PREF_BORDER_CROSSINGS = "show.prehodi";
    val PREF_REGIONALNE_CESTE = "show.regionalne.ceste";
    val PREF_LOKALNE_CESTE = "show.lokalne.ceste";

    val PREF_NOTIFICATIONS = "gcm_enabled";
    val PREF_NOTIFICATIONS_CROSSINGS = "gcm_crossings";
    val PREF_NOTIFICATIONS_HIGHWAYS = "gcm_highways";
    val PREF_NOTIFICATIONS_REGIONAL = "gcm_regional";
    val PREF_NOTIFICATIONS_LOCAL = "gcm_local";

    val listeners : MutableList<OnSettingsChangedListener> = mutableListOf()

    val preferences : SharedPreferences

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    var showHighways: Boolean
        get() = preferences.getBoolean(PREF_AVTOCESTE, true)
        set(value) {
            preferences.edit().putBoolean(PREF_AVTOCESTE, value).apply()
            notifyListeners()
        }

    var showRegionalRoads: Boolean
        get() = preferences.getBoolean(PREF_REGIONALNE_CESTE, true)
        set(value) {
            preferences.edit().putBoolean(PREF_REGIONALNE_CESTE, value).apply()
            notifyListeners()
        }

    var showLocalRoads: Boolean
        get() = preferences.getBoolean(PREF_LOKALNE_CESTE, true)
        set(value) {
            preferences.edit().putBoolean(PREF_LOKALNE_CESTE, value).apply()
            notifyListeners()
        }

    var showBorderCrossings: Boolean
        get() = preferences.getBoolean(PREF_BORDER_CROSSINGS, true)
        set(value) {
            preferences.edit().putBoolean(PREF_BORDER_CROSSINGS, value).apply()
            notifyListeners()
        }

    private fun notifyListeners() {
        for (listener in listeners) listener.onSettingsChanged()
    }

    fun registerChangeListener(listener : OnSettingsChangedListener) {
        listeners.add(listener)
    }

    fun unregisterChangeListener(listener: OnSettingsChangedListener) {
        listeners.remove(listener)
    }

}