package si.virag.promet.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import si.virag.promet.CameraDetailActivity;
import si.virag.promet.Events;
import si.virag.promet.R;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.utils.DataUtils;
import si.virag.promet.utils.LocaleUtil;

@Singleton
public final class PrometMaps implements GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter {

    private static final LatLng MAP_CENTER = new LatLng(46.055556, 14.508333);

    private static boolean markersInitialized = false;
    private static BitmapDescriptor RED_MARKER;
    private static BitmapDescriptor ORANGE_MARKER;
    private static BitmapDescriptor GREEN_MARKER;
    private static BitmapDescriptor YELLOW_MARKER;
    private static BitmapDescriptor AZURE_MARKER;
    private static BitmapDescriptor CONE_MARKER;

    private static BitmapDescriptor CAMERA_MARKER;

    private Context context;
    private GoogleMap map;
    private Map<Marker, String> markerIdMap;

    private Map<String, PrometCamera> cameraMap;
    private final LinkedHashMap<String, Drawable> cameraBitmapMap = new LinkedHashMap<String, Drawable>() {
        @Override
        protected boolean removeEldestEntry(Entry eldest) {
            return this.size() > 10;
        }
    };

    @Inject
    public PrometMaps() {}

    public void setMapInstance(Context ctx, GoogleMap gMap) {
        if (gMap == null)
            return;

        if (!markersInitialized)
            initializeMarkers(ctx);

        this.context = ctx;
        this.map = gMap;

        // Center on Slovenia initially
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTER, 10.0f));
        map.setTrafficEnabled(false);
        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(false);


        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(false);

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10.0f));
                map.setOnMyLocationChangeListener(null);
            }
        });

        map.setOnInfoWindowClickListener(this);
        map.setInfoWindowAdapter(this);

        int fineLocationPermission = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (fineLocationPermission == PermissionChecker.PERMISSION_GRANTED || coarseLocationPermission == PermissionChecker.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        map.setTrafficEnabled(true);
    }

    public void setMapInstanceForDetailView(Context ctx, GoogleMap gMap) {
        if (gMap == null)
            return;

        if (!markersInitialized)
            initializeMarkers(ctx);

        this.map = gMap;

        // Center on Slovenia initially
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(false);

        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setMapToolbarEnabled(false);

        try {
            map.setMyLocationEnabled(false);
        } catch (SecurityException e) {}
    }

    private void initializeMarkers(Context ctx) {
        MapsInitializer.initialize(ctx.getApplicationContext());
        // Marker creation is really slow, so do it only once
        RED_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        ORANGE_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        GREEN_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        YELLOW_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        AZURE_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
        CONE_MARKER = BitmapDescriptorFactory.fromResource(R.drawable.map_cone);

        final Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        final Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3.0f);
        borderPaint.setAntiAlias(true);

        CAMERA_MARKER = BitmapDescriptorFactory.fromBitmap(DataUtils.getBitmapFromVectorDrawable(ctx, R.drawable.ic_camera));
        markersInitialized = true;
    }

    public void showData(final Context context, final List<PrometEvent> prometEvents, List<PrometCamera> prometCameras) {
        if (map == null)
            return;

        map.clear();
        markerIdMap = new HashMap<>();
        cameraMap = new HashMap<>();
        cameraBitmapMap.clear();

        Observable<Pair<String, MarkerOptions>> eventsObservable =  Observable.from(prometEvents)
            .map(event -> {
                MarkerOptions opts = new MarkerOptions();
                opts.position(new LatLng(event.lat, event.lng))
                    .title(LocaleUtil.isSlovenianLocale() ? event.causeSl : event.causeEn)
                    .snippet(LocaleUtil.isSlovenianLocale() ? event.roadNameSl.trim() : event.roadNameEn.trim());

                BitmapDescriptor icon = null;
                if (event.isHighPriority()) {
                    icon = RED_MARKER;
                } else if (event.isRoadworks()) {
                    icon = CONE_MARKER;
                    opts.anchor(0.5f, 0.9f);
                } else if (event.eventGroup == null) {
                    icon = ORANGE_MARKER;
                } else {
                    switch (event.eventGroup) {
                        case AVTOCESTA:
                            icon = GREEN_MARKER;
                            break;
                        case HITRA_CESTA:
                            icon = AZURE_MARKER;
                            break;
                        case MEJNI_PREHOD:
                            icon = ORANGE_MARKER;
                            break;
                        case REGIONALNA_CESTA:
                        case LOKALNA_CESTA:
                            icon = YELLOW_MARKER;
                            break;
                    }
                }

                opts.icon(icon);
                opts.draggable(false);
                opts.zIndex(15.0f);
                return new Pair<>("e" + event.id, opts);
            });

        Observable<Pair<String, MarkerOptions>> camerasObservable = Observable.from(prometCameras)
            .map(prometCamera -> {
                cameraMap.put("c" + prometCamera.id, prometCamera);
                MarkerOptions options = new MarkerOptions()
                           .position(new LatLng(prometCamera.lat, prometCamera.lng))
                           .draggable(false)
                           .anchor(0.5f, 0.5f)
                           .zIndex(5.0f)
                           .title(prometCamera.id)
                           .snippet(prometCamera.getText())
                           .icon(CAMERA_MARKER);
                return new Pair<>("c" + prometCamera.id, options);
            });


        // The order here is important because we don't want markers covering each other in wrong order
        Observable.concat(eventsObservable, camerasObservable)
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(stringMarkerOptionsPair -> {
                      Marker marker = map.addMarker(stringMarkerOptionsPair.second);
                      markerIdMap.put(marker, stringMarkerOptionsPair.first);
                  });
    }

    public void showPoint(LatLng point) {
        if (map == null)
            return;

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 11.0f));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String id = markerIdMap.get(marker);
        if (id == null) return;

        // Resolve to types
        if (id.startsWith("e")) {
            EventBus.getDefault().post(new Events.ShowEventInList(Long.valueOf(id.substring(1))));
        } else if (id.startsWith("c")) {
            PrometCamera camera = cameraMap.get(id);
            if (camera == null) return;
            Intent intent = new Intent(context, CameraDetailActivity.class);
            intent.putExtra("camera", camera);
            context.startActivity(intent);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        if (markerIdMap == null || map == null || context == null) return null;
        final String id = markerIdMap.get(marker);
        if (id == null || !id.startsWith("c")) return null;

        View infoView = LayoutInflater.from(context).inflate(R.layout.info_camera_view, null);
        TextView title = infoView.findViewById(R.id.info_title);
        title.setText(marker.getTitle());

        ImageView imageView = infoView.findViewById(R.id.info_image);
        View loadingView = infoView.findViewById(R.id.info_loading);

        if (cameraBitmapMap.get(id) == null) {
            DataUtils.getCameraImageLoader(context, cameraMap.get(id).getImageLink())
                     .into(new SimpleTarget<GlideDrawable>() {
                         @Override
                         public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                             cameraBitmapMap.put(id, resource);
                             marker.showInfoWindow();
                         }
                     });

            imageView.setVisibility(View.INVISIBLE);
        } else {
            loadingView.setVisibility(View.INVISIBLE);
            imageView.setImageDrawable(cameraBitmapMap.get(id));
        }

        return infoView;
    }
}
