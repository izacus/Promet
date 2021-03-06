package si.virag.promet.fragments.cameras;

import android.view.View;
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
        setExpanded(false);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CameraHeaderItem && ((CameraHeaderItem) o).title.equalsIgnoreCase(title);

    }

    @Override
    public int getExpansionLevel() {
        return 1;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_header;
    }

    @Override
    public CameraHeaderItemHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new CameraHeaderItemHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, CameraHeaderItemHolder holder, int position, List payloads) {
        holder.title.setText(title);
    }

    static class CameraHeaderItemHolder extends ExpandableViewHolder {

        final TextView title;

        CameraHeaderItemHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);
            view.setOnClickListener(l -> toggleExpansion());
            title = view.findViewById(R.id.item_header_title);
        }

        @Override
        protected boolean isViewExpandableOnClick() {
            return true;
        }
    }
}
