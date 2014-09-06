package si.virag.promet.map;

import android.location.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import si.virag.promet.api.model.PrometEvent;

import java.util.List;

public class PrometMaps {

    private static final LatLng MAP_CENTER = new LatLng(46.055556, 14.508333);

    private GoogleMap map;

    public void setMapInstance(GoogleMap gMap) {

        this.map = gMap;

        // Center on Slovenia initially
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTER, 7.0f));
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(false);

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                map.setOnMyLocationChangeListener(null);
            }
        });

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10.0f));
            }
        });

        map.setMyLocationEnabled(true);
    }

    public void showEvents(List<PrometEvent> prometEvents) {
        assert map != null;
        map.clear();

        for (PrometEvent event : prometEvents) {

            float markerColor = 0;

            if (event.roadType == null) {
                markerColor = BitmapDescriptorFactory.HUE_ORANGE;
            }
            else {
                switch (event.roadType) {
                    case AVTOCESTA:
                        markerColor = BitmapDescriptorFactory.HUE_GREEN;
                        break;
                    case HITRA_CESTA:
                        markerColor = BitmapDescriptorFactory.HUE_AZURE;
                        break;
                    case REGIONALNA_CESTA:
                    case LOKALNA_CESTA:
                        markerColor = BitmapDescriptorFactory.HUE_YELLOW;
                        break;
                }
            }


            Marker m = map.addMarker(new MarkerOptions()
                              .position(new LatLng(event.lat, event.lng))
                              .title(event.cause)
                              .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                              .alpha(1.0f - ((float)event.priority / 15.0f))
                              .snippet(event.roadName));

        }
    }
}
