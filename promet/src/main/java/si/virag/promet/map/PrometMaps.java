package si.virag.promet.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;

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
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import si.virag.promet.Events;
import si.virag.promet.R;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.api.model.PrometCounter;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.utils.DataUtils;
import si.virag.promet.utils.LocaleUtil;

public class PrometMaps implements GoogleMap.OnInfoWindowClickListener {

    public static final LatLng MAP_CENTER = new LatLng(46.055556, 14.508333);
    private static final String LOG_TAG = "Promet.Maps";

    private static boolean markersInitialized = false;
    private static BitmapDescriptor RED_MARKER;
    private static BitmapDescriptor ORANGE_MARKER;
    private static BitmapDescriptor GREEN_MARKER;
    private static BitmapDescriptor YELLOW_MARKER;
    private static BitmapDescriptor AZURE_MARKER;
    private static BitmapDescriptor CONE_MARKER;

    private static BitmapDescriptor CAMERA_MARKER;

    private static final int[][] TRAFFIC_DENSITY_COLORS = {
            { Color.TRANSPARENT, Color.TRANSPARENT },  // NO DATA
            { Color.argb(140, 102, 255, 0), Color.argb(64, 102, 255, 0) }, // NORMAL TRAFFIC
            { Color.argb(100, 242, 255, 0), Color.argb(160, 242, 255, 0) }, // INCREASED TRAFFIC
            { Color.argb(180, 255, 208, 0), Color.argb(256, 255, 184, 0) }, // DENSER TRAFFIC
            { Color.argb(180, 255, 18, 0), Color.argb(256, 255, 18, 0) }, // DENSE TRAFFIC
            { Color.argb(200, 255, 0, 0), Color.argb(256, 255, 0, 0)}
    };

    private static BitmapDescriptor[] TRAFFIC_DENSITY_MARKER_BITMAPS;

    private GoogleMap map;
    private Map<Marker, String> markerIdMap;
    private boolean isSlovenianLocale;

    public void setMapInstance(Context ctx, GoogleMap gMap) {
        if (gMap == null)
            return;

        if (!markersInitialized)
            initializeMarkers(ctx);

        this.map = gMap;
        this.isSlovenianLocale = LocaleUtil.isSlovenianLocale(ctx);

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
        this.isSlovenianLocale = LocaleUtil.isSlovenianLocale(ctx);

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
        TRAFFIC_DENSITY_MARKER_BITMAPS = new BitmapDescriptor[TRAFFIC_DENSITY_COLORS.length];
        for (int i = 0; i < TRAFFIC_DENSITY_COLORS.length; i++) {
            int circleRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7.0f, ctx.getResources().getDisplayMetrics());
            final Bitmap bmp = Bitmap.createBitmap(circleRadius * 2, circleRadius * 2, Bitmap.Config.ARGB_8888);
            p.setColor(TRAFFIC_DENSITY_COLORS[i][0]);
            borderPaint.setColor(TRAFFIC_DENSITY_COLORS[i][1]);
            final Canvas c = new Canvas(bmp);
            c.drawCircle(circleRadius, circleRadius, circleRadius, p);
            c.drawCircle(circleRadius, circleRadius, circleRadius, borderPaint);
            TRAFFIC_DENSITY_MARKER_BITMAPS[i] = BitmapDescriptorFactory.fromBitmap(bmp);
        }

        CAMERA_MARKER = BitmapDescriptorFactory.fromBitmap(DataUtils.getBitmapFromVectorDrawable(ctx, R.drawable.ic_camera));
        markersInitialized = true;
    }

    public void showData(final Context context, final List<PrometEvent> prometEvents, final List<PrometCounter> prometCounters, List<PrometCamera> prometCameras) {
        if (map == null)
            return;

        map.clear();
        markerIdMap = new HashMap<>();

        Observable<Pair<String, MarkerOptions>> eventsObservable =  Observable.from(prometEvents)
            .map(new Func1<PrometEvent, Pair<String, MarkerOptions>>() {
              @Override
              public Pair<String, MarkerOptions> call(PrometEvent event) {
                  MarkerOptions opts = new MarkerOptions();
                  opts.position(new LatLng(event.lat, event.lng))
                      .title(isSlovenianLocale ? event.cause : event.causeEn)
                      .snippet(event.roadName);

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
              }
            });

        Observable<Pair<String, MarkerOptions>> countersObservable = Observable.from(prometCounters)
            .map(new Func1<PrometCounter, Pair<String, MarkerOptions>>() {
                @Override
                public Pair<String, MarkerOptions> call(PrometCounter event) {
                    MarkerOptions options = new MarkerOptions()
                            .position(new LatLng(event.lat, event.lng))
                            .anchor(0.5f, 0.5f)
                            .flat(true)
                            .draggable(false)
                            .title(context.getResources().getStringArray(R.array.traffic_status_strings)[event.status.ordinal()] + " - " + event.locationName)
                            .snippet(context.getResources().getString(R.string.map_traffic_detail, event.avgSpeed, event.gap))
                            .icon(TRAFFIC_DENSITY_MARKER_BITMAPS[event.status.ordinal()]);
                    return new Pair<>("t" + event.id, options);
                }
            });

        Observable<Pair<String, MarkerOptions>> camerasObservable = Observable.from(prometCameras)
            .map(new Func1<PrometCamera, Pair<String, MarkerOptions>>() {
              @Override
              public Pair<String, MarkerOptions> call(PrometCamera prometCamera) {
                  MarkerOptions options = new MarkerOptions()
                             .position(new LatLng(prometCamera.lat, prometCamera.lng))
                             .draggable(false)
                             .anchor(0.5f, 0.5f)
                             .zIndex(5.0f)
                             .title(prometCamera.title)
                             .snippet(prometCamera.summary)
                             .icon(CAMERA_MARKER);
                  return new Pair<>("c" + prometCamera.id, options);
              }
            });


        // The order here is important because we don't want markers covering each other in wrong order
        Observable.concat(eventsObservable, camerasObservable, countersObservable)
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Action1<Pair<String, MarkerOptions>>() {
                      @Override
                      public void call(Pair<String, MarkerOptions> stringMarkerOptionsPair) {
                          Marker marker = map.addMarker(stringMarkerOptionsPair.second);
                          markerIdMap.put(marker, stringMarkerOptionsPair.first);
                      }
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
        }
    }
}
