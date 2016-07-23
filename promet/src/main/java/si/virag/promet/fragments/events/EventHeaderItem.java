package si.virag.promet.fragments.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import si.virag.promet.R;

public class EventHeaderItem extends AbstractHeaderItem<EventHeaderItem.EventHeaderItemHolder> {

    private final String title;

    public EventHeaderItem(String title) {
        super();
        this.title = title;
        setHidden(false);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EventHeaderItem && ((EventHeaderItem) o).title.equals(title);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_header;
    }

    @Override
    public EventHeaderItemHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new EventHeaderItemHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, EventHeaderItemHolder holder, int position, List payloads) {
        holder.title.setText(title);
    }

    static class EventHeaderItemHolder extends FlexibleViewHolder {
        final TextView title;

        EventHeaderItemHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            title = (TextView) view.findViewById(R.id.item_header_title);
        }
    }
}
