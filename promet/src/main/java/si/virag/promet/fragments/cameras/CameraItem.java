package si.virag.promet.fragments.cameras;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import si.virag.promet.CameraDetailActivity;
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
    public CameraItemHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new CameraItemHolder(view, adapter);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, final CameraItemHolder holder, int position, List payloads) {
        holder.titleText.setText(camera.title.substring(0, 1).toUpperCase() + camera.title.substring(1));
        holder.locationText.setText(camera.getText());

        if (!isHidden()) {
            holder.cameraView.setCamera(camera);
            holder.setMapLocation(new LatLng(camera.lat, camera.lng));
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(holder.container.getContext(), CameraDetailActivity.class);
                i.putExtra("camera", camera);
                ViewCompat.setTransitionName(holder.cameraView, "cameraView");
                ViewCompat.setTransitionName(holder.mapView, "mapView");
                holder.container.getContext().startActivity(i);
            }
        });
    }

    static class CameraItemHolder extends FlexibleViewHolder implements OnMapReadyCallback {

        final View container;
        final TextView titleText;
        final TextView locationText;
        final CameraView cameraView;
        final MapView mapView;

        GoogleMap map;
        LatLng location;

        CameraItemHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            container = view;
            titleText = (TextView) view.findViewById(R.id.item_camera_title);
            locationText = (TextView) view.findViewById(R.id.item_camera_location);
            cameraView = (CameraView) view.findViewById(R.id.item_camera_view);
            mapView = (MapView) view.findViewById(R.id.item_camera_map);
            mapView.onCreate(null);
            mapView.getMapAsync(this);
        }

        public void setMapLocation(LatLng location) {
            this.location = location;
            if (map != null) {
                showPointOnMap();
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            this.map = googleMap;
            map.setBuildingsEnabled(false);
            map.setIndoorEnabled(false);

            try {
                map.setMyLocationEnabled(false);
            } catch (SecurityException ignored) {}

            map.setTrafficEnabled(false);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setIndoorLevelPickerEnabled(false);
            map.getUiSettings().setMapToolbarEnabled(false);

            if (location != null) {
                showPointOnMap();
            }
        }

        private void showPointOnMap() {
            map.clear();
            map.addCircle(new CircleOptions()
                              .center(location)
                              .fillColor(Color.argb(160, 255, 0, 0))
                              .strokeColor(Color.RED)
                              .strokeWidth(1.0f)
                              .radius(650));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 9f));
        }


    }
}
