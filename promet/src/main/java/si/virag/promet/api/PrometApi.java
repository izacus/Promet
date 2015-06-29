package si.virag.promet.api;


import java.util.List;

import rx.Observable;
import si.virag.promet.api.model.PrometCounter;
import si.virag.promet.api.model.PrometEvent;

public abstract class PrometApi {

    public abstract Observable<List<PrometEvent>> getReloadPrometEvents();
    public abstract Observable<List<PrometEvent>> getPrometEvents();
    public abstract Observable<List<PrometCounter>> getPrometCounters();

}
