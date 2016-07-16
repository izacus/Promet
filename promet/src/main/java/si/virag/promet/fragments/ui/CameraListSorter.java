package si.virag.promet.fragments.ui;

import rx.functions.Func2;
import si.virag.promet.api.model.PrometCamera;

public class CameraListSorter implements Func2<PrometCamera, PrometCamera, Integer> {

    @Override
    public Integer call(PrometCamera lhs, PrometCamera rhs) {
        if (!lhs.group.equalsIgnoreCase(rhs.group)) {
            return lhs.group.compareTo(rhs.group);
        }

        if (!lhs.region.equalsIgnoreCase(rhs.region)) {
            return lhs.region.compareTo(rhs.region);
        }

        return lhs.title.compareTo(rhs.title);
    }
}
