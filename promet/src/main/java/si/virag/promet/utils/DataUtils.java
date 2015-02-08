package si.virag.promet.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.crashlytics.android.Crashlytics;

import si.virag.promet.api.model.RoadType;

public class DataUtils {
    
    public static RoadType roadPriorityToRoadType(int priority, boolean isCrossing) {
        if (isCrossing) {
            return RoadType.MEJNI_PREHOD;
        } else if (priority < 6) {
            return RoadType.AVTOCESTA;
        } else if (priority < 13) {
            return RoadType.HITRA_CESTA;
        } else if (priority < 17) {
            return RoadType.REGIONALNA_CESTA;
        } else {
            return RoadType.LOKALNA_CESTA;
        }
    }

    public static String getUserAgent(Context context) {
        int appVersion = 0;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
        }

        final String header = String.format("Promet/%d %s/%s/%s Android %s/%d (%s)", appVersion,
                Build.MANUFACTURER,
                Build.MODEL,
                Build.DEVICE,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                Build.FINGERPRINT);

        return header;
    }
}
