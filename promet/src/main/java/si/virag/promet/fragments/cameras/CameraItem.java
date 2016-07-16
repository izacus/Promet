package si.virag.promet.fragments.cameras;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import si.virag.promet.R;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.fragments.ui.CameraView;

public class CameraItem extends AbstractSectionableItem<CameraItem.CameraItemHolder, CameraHeaderItem> {

    @NonNull
    public final PrometCamera camera;

    public CameraItem(@NonNull CameraHeaderItem header, @NonNull PrometCamera camera) {
        super(header);
        this.camera = camera;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CameraItem && ((CameraItem) o).camera.id == camera.id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_camera;
    }

    @Override
    public CameraItemHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new CameraItemHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, CameraItemHolder holder, int position, List payloads) {
        holder.title.setText(camera.title.substring(0, 1).toUpperCase() + camera.title.substring(1));
        holder.location.setText(camera.summary);

        if (!isHidden()) {
            holder.cameraView.setCamera(camera);
        }
    }

    static class CameraItemHolder extends FlexibleViewHolder {

        final TextView title;
        final TextView location;
        final CameraView cameraView;

        CameraItemHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            title = (TextView) view.findViewById(R.id.item_camera_title);
            location = (TextView) view.findViewById(R.id.item_camera_location);
            cameraView = (CameraView) view.findViewById(R.id.item_camera_view);
        }


    }
}
