package si.virag.promet.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.location.Location;
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
import si.virag.promet.api.model.PrometCounter;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.utils.LocaleUtil;

public class PrometMaps implements GoogleMap.OnInfoWindowClickListener {

    public static final LatLng MAP_CENTER = new LatLng(46.055556, 14.508333);

    private static boolean markersInitialized = false;
    private static BitmapDescriptor RED_MARKER;
    private static BitmapDescriptor ORANGE_MARKER;
    private static BitmapDescriptor GREEN_MARKER;
    private static BitmapDescriptor YELLOW_MARKER;
    private static BitmapDescriptor AZURE_MARKER;
    private static BitmapDescriptor CONE_MARKER;

    private static final int[][] TRAFFIC_DENSITY_COLORS = {
            { Color.TRANSPARENT, Color.TRANSPARENT },  // NO DATA
            { Color.argb(240, 102, 255, 0), Color.argb(64, 102, 255, 0) }, // NORMAL TRAFFIC
            { Color.argb(240, 242, 255, 0), Color.argb(64, 242, 255, 0) }, // INCREASED TRAFFIC
            { Color.argb(240, 255, 208, 0), Color.argb(64, 255, 208, 0) }, // DENSER TRAFFIC
            { Color.argb(240, 255, 119, 0), Color.argb(64, 255, 119, 0) }, // DENSE TRAFFIC
            { Color.argb(256, 255, 0, 0), Color.argb(128, 255, 0, 0)}
    };

    private static BitmapDescriptor[] TRAFFIC_DENSITY_MARKER_BITMAPS;

    private GoogleMap map;
    private Map<Marker, Long> markerIdMap;
    private boolean isSlovenianLocale;

    public void setMapInstance(Context ctx, GoogleMap gMap) {
        if (gMap == null)
            return;

        if (!markersInitialized)
            initializeMarkers(ctx);

        this.map = gMap;
        this.isSlovenianLocale = LocaleUtil.isSlovenianLocale(ctx);

        // Center on Slovenia initially
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTER, 7.0f));
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

        map.setMyLocationEnabled(true);

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
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(Color.WHITE);
        TRAFFIC_DENSITY_MARKER_BITMAPS = new BitmapDescriptor[TRAFFIC_DENSITY_COLORS.length];
        for (int i = 0; i < TRAFFIC_DENSITY_COLORS.length; i++) {
            int circleRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0f, ctx.getResources().getDisplayMetrics());
            final Bitmap bmp = Bitmap.createBitmap(circleRadius * 2, circleRadius * 2, Bitmap.Config.ARGB_8888);
            p.setShader(new RadialGradient(circleRadius,
                    circleRadius,
                    circleRadius,
                    TRAFFIC_DENSITY_COLORS[i][0],
                    TRAFFIC_DENSITY_COLORS[i][1],
                    Shader.TileMode.CLAMP));
            final Canvas c = new Canvas(bmp);
            c.drawCircle(circleRadius, circleRadius, circleRadius, p);
            TRAFFIC_DENSITY_MARKER_BITMAPS[i] = BitmapDescriptorFactory.fromBitmap(bmp);
        }

        markersInitialized = true;
    }

    public void showEvents(List<PrometEvent> prometEvents, final List<PrometCounter> prometCounters) {
        if (map == null)
            return;

        map.clear();
        markerIdMap = new HashMap<>();

        Observable.from(prometEvents)
                  .map(new Func1<PrometEvent, Pair<Long, MarkerOptions>>() {
                      @Override
                      public Pair<Long, MarkerOptions> call(PrometEvent event) {
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
                          return new Pair<>(event.id, opts);
                      }
                  })
                  .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<Long, MarkerOptions>>() {
                    @Override
                    public void call(Pair<Long, MarkerOptions> idMarkerPair) {
                        Marker m = map.addMarker(idMarkerPair.second);
                          markerIdMap.put(m, idMarkerPair.first);
                      }
                  });


        Observable.from(prometCounters)
                .map(new Func1<PrometCounter, MarkerOptions>() {
                    @Override
                    public MarkerOptions call(PrometCounter event) {
                        return new MarkerOptions()
                                .position(new LatLng(event.lat, event.lng))
                                .anchor(0.5f, 0.5f)
                                .flat(true)
                                .draggable(false)
                                .icon(TRAFFIC_DENSITY_MARKER_BITMAPS[event.status.ordinal()]);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MarkerOptions>() {
                    @Override
                    public void call(MarkerOptions marker) {
                        map.addMarker(marker);
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
        Long id = markerIdMap.get(marker);
        if (id == null) return;

        EventBus.getDefault().post(new Events.ShowEventInList(id));
    }
}
