package si.virag.promet.utils;

import android.content.Context;

import com.franmontiel.localechanger.LocaleChanger;

public class LocaleUtil {

    public static boolean isSlovenianLocale() {
        return LocaleChanger.getLocale().getLanguage().equals("sl");
    }
}
