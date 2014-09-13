package si.virag.promet.fragments.ui;

import rx.functions.Func1;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.utils.PrometSettings;

public class EventListFilter implements Func1<PrometEvent, Boolean> {
    private final PrometSettings prometSettings;

    public EventListFilter(PrometSettings settings) {
        this.prometSettings = settings;
    }

    @Override
    public Boolean call(PrometEvent prometEvent) {

        if (prometEvent.roadType == null)
            return true;

        switch (prometEvent.roadType) {
            case AVTOCESTA:
            case HITRA_CESTA:
                return prometSettings.getShowAvtoceste();
            case MEJNI_PREHOD:
                return prometSettings.getShowBorderCrossings();
            case REGIONALNA_CESTA:
                return prometSettings.getShowRegionalneCeste();
            case LOKALNA_CESTA:
                return prometSettings.getShowLokalneCeste();

            default:
                return true;
        }
    }
}
