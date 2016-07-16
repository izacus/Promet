package si.virag.promet.fragments.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import si.virag.promet.R;
import si.virag.promet.api.model.PrometCamera;

public class CamerasAdapter extends RecyclerView.Adapter<CamerasAdapter.CameraViewHolder> {

    @NonNull
    private List<PrometCamera> cameras;

    public CamerasAdapter(@NonNull List<PrometCamera> cameraList) {
        this.cameras = cameraList;
        setHasStableIds(true);
    }

    @Override
    public CameraViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera, parent, false);
        return new CameraViewHolder(view);
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onBindViewHolder(CameraViewHolder holder, int position) {
        PrometCamera camera = cameras.get(position);
        holder.title.setText(camera.title.substring(0, 1).toUpperCase() + camera.title.substring(1));
        holder.location.setText(camera.summary);
        holder.cameraView.setCamera(camera);
    }

    @Override
    public int getItemCount() {
        return cameras.size();
    }

    @Override
    public long getItemId(int position) {
        return cameras.get(position).id;
    }

    public void setData(@NonNull List<PrometCamera> cameraList) {
        this.cameras = cameraList;
        notifyDataSetChanged();
    }

    static class CameraViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView location;
        final CameraView cameraView;

        CameraViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_camera_title);
            location = (TextView) itemView.findViewById(R.id.item_camera_location);
            cameraView = (CameraView) itemView.findViewById(R.id.item_camera_view);
        }
    }
}
