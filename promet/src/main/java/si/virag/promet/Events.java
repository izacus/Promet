package si.virag.promet;

import com.google.android.gms.maps.model.LatLng;

public class Events {

    public static class RefreshStarted {}

    public static class RefreshCompleted {}

    public static class ShowPointOnMap {
        public final LatLng point;

        public ShowPointOnMap(LatLng point) {
            this.point = point;
        }
    }

    public static class ShowEventInList {
        public final long id;

        public ShowEventInList(long id) {
            this.id = id;
        }
    }
}