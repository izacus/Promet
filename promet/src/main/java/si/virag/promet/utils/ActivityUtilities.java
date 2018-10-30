package si.virag.promet.utils;

import android.content.res.Configuration;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import si.virag.promet.R;

public class ActivityUtilities {

    public static void setupTransluscentNavigation(AppCompatActivity activity) {
        // Transluscent navigation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = activity.getWindow(); // in Activity's onCreate() for instance

            if (activity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else {
                w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }

            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public static SystemBarTintManager setupSystembarTint(AppCompatActivity activity, Toolbar toolbar) {
        // Set titlebar tint
        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(ContextCompat.getColor(activity, R.color.theme_color));

        // Setup top margin for toolbar when its transparent
        LinearLayout.LayoutParams toolbarLayoutParams = (LinearLayout.LayoutParams) toolbar.getLayoutParams();
        toolbarLayoutParams.topMargin = tintManager.getConfig().getPixelInsetTop(false);
        toolbar.setLayoutParams(toolbarLayoutParams);
        return tintManager;
    }
}
