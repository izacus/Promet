package si.virag.promet.fragments.events;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import de.greenrobot.event.EventBus;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import si.virag.fuzzydatetime.FuzzyDateTimeFormatter;
import si.virag.promet.Events;
import si.virag.promet.R;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.utils.LocaleUtil;


public class EventItem extends AbstractSectionableItem<EventItem.EventItemHolder, EventHeaderItem> {

    @NonNull
    private final PrometEvent event;

    public EventItem(@NonNull EventHeaderItem header, @NonNull PrometEvent event) {
        super(header);
        this.event = event;

    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PrometEvent && ((PrometEvent) o).id == event.id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_event;
    }

    @Override
    public EventItemHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new EventItemHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, EventItemHolder holder, int position, List payloads) {
        SpannableString titleText = new SpannableString(event.cause);
        SpannableString locationText = new SpannableString(event.roadName.trim());
        if (event.isHighPriority()) {
            int color = ContextCompat.getColor(holder.view.getContext(), android.R.color.holo_red_dark);
            titleText.setSpan(new ForegroundColorSpan(color), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            locationText.setSpan(new ForegroundColorSpan(color), 0, locationText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        holder.titleView.setText(titleText);
        holder.descriptionView.setText(event.description);
        holder.locationView.setText(locationText);
        holder.timeView.setVisibility(View.INVISIBLE);
        /*holder.timeView.setVisibility(event.validFrom == null ? View.INVISIBLE : View.VISIBLE);
        holder.timeView.setText(event.validFrom == null ? "" : FuzzyDateTimeFormatter.getTimeAgo(holder.view.getContext(), event.validFrom.toLocalDateTime())); */
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new Events.ShowPointOnMap(new LatLng(event.lat, event.lng)));
            }
        });
    }

    @NonNull
    public PrometEvent getEvent() {
        return event;
    }

    public static class EventItemHolder extends FlexibleViewHolder {

        public View view;
        public TextView titleView;
        public TextView descriptionView;
        public TextView timeView;
        public TextView locationView;

        public EventItemHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.view = view.findViewById(R.id.item_event);
            this.titleView = (TextView) view.findViewById(R.id.item_event_title);
            this.descriptionView = (TextView) view.findViewById(R.id.item_event_description);
            this.timeView = (TextView)view.findViewById(R.id.item_event_time);
            this.locationView = (TextView)view.findViewById(R.id.item_event_location);
        }
    }
}
