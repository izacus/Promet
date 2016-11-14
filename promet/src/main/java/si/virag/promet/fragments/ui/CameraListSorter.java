package si.virag.promet.fragments.ui;

import rx.functions.Func2;
import si.virag.promet.api.model.PrometCamera;

public class CameraListSorter implements Func2<PrometCamera, PrometCamera, Integer> {

    @Override
    public Integer call(PrometCamera lhs, PrometCamera rhs) {

        if (!lhs.getRegion().equalsIgnoreCase(rhs.getRegion())) {
            return lhs.getRegion().compareTo(rhs.getRegion());
        }

        return lhs.title.compareTo(rhs.title);
    }
}
