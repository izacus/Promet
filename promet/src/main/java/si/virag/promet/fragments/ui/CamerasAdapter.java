package si.virag.promet.fragments.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.List;

import si.virag.promet.R;
import si.virag.promet.api.model.PrometCamera;

public class CamerasAdapter extends ExpandableRecyclerAdapter<CamerasAdapter.CameraCategoryViewHolder, CamerasAdapter.CameraViewHolder> {

    @NonNull
    private final LayoutInflater inflater;

    public CamerasAdapter(@NonNull Context context, @NonNull List<CameraCategory> cameraList) {
        super(cameraList);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public CameraCategoryViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View view = inflater.inflate(R.layout.item_camera_header, parentViewGroup, false);
        return new CameraCategoryViewHolder(view);
    }

    @Override
    public CameraViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View view = inflater.inflate(R.layout.item_camera, childViewGroup, false);
        return new CameraViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(CameraCategoryViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
        CameraCategory category = (CameraCategory)parentListItem;
        parentViewHolder.title.setText(category.title);
    }

    @Override
    public void onBindChildViewHolder(CameraViewHolder childViewHolder, int position, Object childListItem) {
        PrometCamera camera = (PrometCamera) childListItem;
        childViewHolder.title.setText(camera.title);
    }

    public static class CameraCategoryViewHolder extends ParentViewHolder {

        final TextView title;

        public CameraCategoryViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_camera_header_title);
        }
    }

    public static class CameraViewHolder extends ChildViewHolder {

        final TextView title;

        public CameraViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_camera_title);
        }
    }
}
