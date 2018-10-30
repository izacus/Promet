package si.virag.promet.fragments.events;


import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import de.greenrobot.event.EventBus;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.viewholders.FlexibleViewHolder;
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
    public EventItemHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new EventItemHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, EventItemHolder holder, int position, List payloads) {
        boolean isSlovenianLocale = LocaleUtil.isSlovenianLocale();

        SpannableString titleText = new SpannableString(isSlovenianLocale ? event.causeSl : event.causeEn);
        SpannableString locationText = new SpannableString(isSlovenianLocale ? event.roadNameSl.trim() : event.roadNameEn);
        if (event.isHighPriority()) {
            int color = ContextCompat.getColor(holder.view.getContext(), android.R.color.holo_red_dark);
            titleText.setSpan(new ForegroundColorSpan(color), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            locationText.setSpan(new ForegroundColorSpan(color), 0, locationText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        holder.titleView.setText(titleText);
        holder.descriptionView.setText(isSlovenianLocale ? event.descriptionSl : event.descriptionEn);
        holder.locationView.setText(locationText);
        holder.timeView.setVisibility(event.updated == null ? View.INVISIBLE : View.VISIBLE);
        holder.timeView.setText(DateUtils.getRelativeTimeSpanString(event.updated.toInstant().toEpochMilli(),
                                                                    System.currentTimeMillis(),
                                                                    DateUtils.MINUTE_IN_MILLIS,
                                                                    DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY).toString().toLowerCase());
        holder.view.setOnClickListener(v -> EventBus.getDefault().post(new Events.ShowPointOnMap(new LatLng(event.lat, event.lng))));
    }

    @NonNull
    public PrometEvent getEvent() {
        return event;
    }

    public static class EventItemHolder extends FlexibleViewHolder {

        public View view;
        TextView titleView;
        TextView descriptionView;
        TextView timeView;
        TextView locationView;

        private EventItemHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.view = view.findViewById(R.id.item_event);
            this.titleView = view.findViewById(R.id.item_event_title);
            this.descriptionView = view.findViewById(R.id.item_event_description);
            this.timeView = view.findViewById(R.id.item_event_time);
            this.locationView = view.findViewById(R.id.item_event_location);
        }
    }
}
