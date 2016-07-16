package si.virag.promet;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.map.PrometMaps;
import si.virag.promet.utils.ActivityUtilities;

/**
 * This activity shows a larger view of the camera.
 */
public class CameraDetailActivity extends AppCompatActivity implements OnMapReadyCallback {


    @NonNull
    private Toolbar toolbar;

    @NonNull
    private PrometCamera camera;

    @NonNull
    private ImageView cameraImage;

    @NonNull
    private TextView summaryText;

    @NonNull
    private MapView mapView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ActivityUtilities.setupTransluscentNavigation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_detail);

        // Camera is required!
        camera = getIntent().getParcelableExtra("camera");
        if (camera == null) {
            finish();
            return;
        }

        toolbar = (Toolbar) findViewById(R.id.camera_detail_toolbar);
        setSupportActionBar(toolbar);
        ActivityUtilities.setupSystembarTint(this, toolbar);

        cameraImage = (ImageView)findViewById(R.id.camera_detail_image);
        summaryText = (TextView)findViewById(R.id.camera_detail_summary);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
            summaryText.setText(camera.title.substring(0, 1).toUpperCase() + camera.title.substring(1) + " - " + camera.summary);
        } else {
            getSupportActionBar().setTitle(camera.title.substring(0, 1).toUpperCase() + camera.title.substring(1));
            summaryText.setText(camera.summary);
        }

        mapView = (MapView)findViewById(R.id.camera_detail_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        Glide.with(this)
                .load(camera.imageLink)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(cameraImage);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setIndoorLevelPickerEnabled(false);
        map.setIndoorEnabled(false);
        map.setTrafficEnabled(true);
        map.setBuildingsEnabled(true);

        map.clear();
        LatLng location = new LatLng(camera.lat, camera.lng);
        map.addMarker(new MarkerOptions().position(location));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f));
    }
}
