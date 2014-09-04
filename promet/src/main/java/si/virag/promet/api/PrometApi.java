package si.virag.promet.api;


import com.google.common.collect.ImmutableList;
import rx.Observable;
import si.virag.promet.api.model.PrometEvent;

import java.util.List;

public abstract class PrometApi {

    public abstract Observable<ImmutableList<PrometEvent>> getPrometEvents();

}
