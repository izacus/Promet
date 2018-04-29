package si.virag.promet.utils;

import android.content.Context;

import com.franmontiel.localechanger.LocaleChanger;

import java.util.Locale;

import si.virag.promet.R;

public class LocaleUtil {

    public static boolean isSlovenianLocale(Context ctx) {
        return LocaleChanger.getLocale().getLanguage().equals("sl");
    }
}
