package si.virag.promet.fragments.ui;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.utils.DataUtils;

public class CameraView extends AppCompatImageView {

    private static final String LOG_TAG = "Promet.CameraView";
    /** Actual camera **/
    @Nullable
    private PrometCamera camera;

    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, (int)((width / 4.0) * 3.0));
    }

    public void setCamera(@Nullable PrometCamera camera) {
        this.camera = camera;
        if (camera != null) {
            Log.d(LOG_TAG, "Loading camera " + camera.id + " from " + camera.getImageLink());
            DataUtils.getCameraImageLoader(getContext(), camera.getImageLink())
                     .into(this);
        }
    }
}
