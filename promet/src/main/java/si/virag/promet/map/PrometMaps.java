package si.virag.promet.map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import si.virag.promet.api.model.PrometEvent;

import java.util.List;

public class PrometMaps {

    private GoogleMap map;

    public void setMapInstance(GoogleMap map) {

        if (this.map == null) {
            this.map = map;

            // Center on Slovenia initially
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.055556, 14.508333), 7.0f));
            map.setTrafficEnabled(true);
        }

    }

    public void showEvents(List<PrometEvent> prometEvents) {
        assert map != null;

        for (PrometEvent event : prometEvents) {
            Marker m = map.addMarker(new MarkerOptions()
                              .position(new LatLng(event.lat, event.lng))
                              .title(event.cause)
                              .snippet(event.roadName));

        }
    }
}
