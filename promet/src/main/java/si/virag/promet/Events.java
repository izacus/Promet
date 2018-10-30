package si.virag.promet;

import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.LocalDateTime;

import androidx.annotation.Nullable;

public class Events {

    public static class RefreshStarted {}

    public static class RefreshCompleted {
        @Nullable
        public final LocalDateTime lastUpdateTime;

        public RefreshCompleted(@Nullable LocalDateTime lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        public RefreshCompleted() {
            this.lastUpdateTime = null;
        }
    }

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

    public static class UpdateMap {}

    public static class UpdateEventList {
    }

    public static class UpdateActivityHeader {}
}
