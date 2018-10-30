package si.virag.promet.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.crashlytics.android.Crashlytics;

import org.threeten.bp.ZonedDateTime;

import java.util.Locale;

import androidx.core.content.ContextCompat;
import si.virag.promet.api.model.EventGroup;

public class DataUtils {

    public static EventGroup roadPriorityToRoadType(int priority, boolean isCrossing) {
        if (isCrossing) {
            return EventGroup.MEJNI_PREHOD;
        } else if (priority < 6) {
            return EventGroup.AVTOCESTA;
        } else if (priority < 13) {
            return EventGroup.HITRA_CESTA;
        } else if (priority < 17) {
            return EventGroup.REGIONALNA_CESTA;
        } else {
            return EventGroup.LOKALNA_CESTA;
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

        return String.format(Locale.US, "Promet/%d %s/%s/%s Android %s/%d (%s)", appVersion,
                Build.MANUFACTURER,
                Build.MODEL,
                Build.DEVICE,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                Build.FINGERPRINT);
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId).mutate();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static DrawableRequestBuilder<String> getCameraImageLoader(Context context, String url) {
        // We mix in time rounded down to 5 minute segments to timeout cache after 5 mins
        ZonedDateTime time = ZonedDateTime.now();
        String key = String.valueOf(time.getHour()) + String.valueOf((time.getMinute() / 5) * 5);

        return Glide.with(context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .signature(new StringSignature(String.valueOf(key)));
    }
}
