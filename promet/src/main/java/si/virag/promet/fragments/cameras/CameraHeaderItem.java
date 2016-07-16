package si.virag.promet.fragments.cameras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem;
import eu.davidea.viewholders.ExpandableViewHolder;
import si.virag.promet.R;

public class CameraHeaderItem extends AbstractExpandableHeaderItem<CameraHeaderItem.CameraHeaderItemHolder, CameraItem> {

    public final String title;

    public CameraHeaderItem(String title) {
        super();
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CameraHeaderItem) {
            return ((CameraHeaderItem) o).title.equalsIgnoreCase(title);
        }

        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_camera_header;
    }

    @Override
    public CameraHeaderItemHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new CameraHeaderItemHolder(inflater.inflate(getLayoutRes(), parent, false), adapter, true);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, CameraHeaderItemHolder holder, int position, List payloads) {
        holder.title.setText(title);
    }

    static class CameraHeaderItemHolder extends ExpandableViewHolder {

        final TextView title;

        CameraHeaderItemHolder(View view, FlexibleAdapter adapter, boolean stickyHeader) {
            super(view, adapter, stickyHeader);
            title = (TextView) view.findViewById(R.id.item_camera_header_title);
        }
    }
}
