package si.virag.promet.utils;

import android.content.Context;

import java.util.Locale;

public class LocaleUtil {

    public static boolean isSlovenianLocale(Context ctx) {
        Locale locale = ctx.getApplicationContext().getResources().getConfiguration().locale;
        return locale.getLanguage().equalsIgnoreCase("sl") ||
               locale.getLanguage().equalsIgnoreCase("sl-si");
    }
}
