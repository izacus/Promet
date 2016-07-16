package si.virag.promet.fragments.ui;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import si.virag.promet.api.model.PrometCamera;

public class CameraView extends ImageView {

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
            Glide.with(getContext())
                 .load(camera.imageLink)
                 .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                 .into(this);
        }
    }
}
