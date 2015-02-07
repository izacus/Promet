package si.virag.promet.utils;

import si.virag.promet.api.model.RoadType;

public class DataUtils {
    
    public static RoadType roadPriorityToRoadType(int priority, boolean isCrossing) {
        if (isCrossing) {
            return RoadType.MEJNI_PREHOD;
        } else if (priority < 6) {
            return RoadType.AVTOCESTA;
        } else if (priority < 13) {
            return RoadType.HITRA_CESTA;
        } else if (priority < 17) {
            return RoadType.REGIONALNA_CESTA;
        } else {
            return RoadType.LOKALNA_CESTA;
        }
    }
}
