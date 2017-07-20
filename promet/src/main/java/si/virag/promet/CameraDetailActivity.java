package si.virag.promet;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import si.virag.promet.api.data.PrometApi;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.TrafficInfo;
import si.virag.promet.map.PrometMaps;
import si.virag.promet.utils.ActivityUtilities;
import si.virag.promet.utils.DataUtils;

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

    @Inject PrometMaps prometMaps;

    @Inject
    PrometApi prometApi;

    private GoogleMap map;
    private List<PrometEvent> events;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ActivityUtilities.setupTransluscentNavigation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_detail);
        PrometApplication application = (PrometApplication) getApplication();
        application.component().inject(this);

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
            summaryText.setText(camera.getText());
        } else {
            getSupportActionBar().setTitle(camera.id);
            summaryText.setText(camera.getText());
        }

        mapView = (MapView)findViewById(R.id.camera_detail_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        DataUtils.getCameraImageLoader(this, camera.getImageLink())
                 .into(cameraImage);

        prometApi.getTrafficInfo()
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new Subscriber<TrafficInfo>() {
                     @Override
                     public void onCompleted() {}

                     @Override
                     public void onError(Throwable e) {}

                     @Override
                     public void onNext(TrafficInfo trafficInfo) {
                         events = trafficInfo.events;
                         displayMapMarkers();
                     }
                 });
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

    private void displayMapMarkers() {
        if (map == null) return;

        map.clear();
        if (events != null) {
            prometMaps.showData(this, events, Collections.singletonList(camera));
        }

        LatLng location = new LatLng(camera.lat, camera.lng);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        prometMaps.setMapInstanceForDetailView(this, map);
        displayMapMarkers();
    }
}
