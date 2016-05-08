package si.virag.promet.fragments.ui;

import android.support.annotation.NonNull;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.List;

import si.virag.promet.api.model.PrometCamera;

public final class CameraCategory implements ParentListItem {

    final String title;
    final List<PrometCamera> cameras;

    public CameraCategory(@NonNull String title, @NonNull List<PrometCamera> cameras) {
        this.title = title;
        this.cameras = cameras;
    }

    @Override
    public List<PrometCamera> getChildItemList() {
        return cameras;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
