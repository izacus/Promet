package si.virag.promet.map;

import android.content.Context;
import android.location.Location;
import android.util.Pair;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import si.virag.promet.Events;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.utils.LocaleUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrometMaps implements GoogleMap.OnInfoWindowClickListener {

    private static final LatLng MAP_CENTER = new LatLng(46.055556, 14.508333);

    private static boolean markersInitialized = false;
    private static BitmapDescriptor RED_MARKER;
    private static BitmapDescriptor ORANGE_MARKER;
    private static BitmapDescriptor GREEN_MARKER;
    private static BitmapDescriptor YELLOW_MARKER;
    private static BitmapDescriptor AZURE_MARKER;

    private GoogleMap map;
    private Map<Marker, Long> markerIdMap;
    private boolean isSlovenianLocale;

    public void setMapInstance(Context ctx, GoogleMap gMap) {
        if (gMap == null)
            return;

        if (!markersInitialized)
            initializeMarkers();

        this.map = gMap;
        this.isSlovenianLocale = LocaleUtil.isSlovenianLocale(ctx);

        // Center on Slovenia initially
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTER, 7.0f));
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(false);

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

    private void initializeMarkers() {
        // Marker creation is really slow, so do it only once
        RED_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        ORANGE_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        GREEN_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        YELLOW_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        AZURE_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
        markersInitialized = true;
    }

    public void showEvents(List<PrometEvent> prometEvents) {
        if (map == null)
            return;

        map.clear();
        markerIdMap = new HashMap<>();

        Observable.from(prometEvents)
                  .map(new Func1<PrometEvent, Pair<Long, MarkerOptions>>() {
                      @Override
                      public Pair<Long, MarkerOptions> call(PrometEvent event) {

                          BitmapDescriptor icon = null;
                          if (event.isHighPriority()) {
                              icon = RED_MARKER;
                          }
                          else if (event.roadType == null) {
                              icon = ORANGE_MARKER;
                          }
                          else {
                              switch (event.roadType) {
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

                          return new Pair<>(event.id, new MarkerOptions()
                                  .position(new LatLng(event.lat, event.lng))
                                  .title(isSlovenianLocale ? event.cause : event.causeEn)
                                  .icon(icon)
                                  .snippet(event.roadName));
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
