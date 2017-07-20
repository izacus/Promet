package si.virag.promet.fragments.ui;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import si.virag.promet.Events;
import si.virag.promet.R;
import si.virag.promet.api.model.EventGroup;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.utils.LocaleUtil;

public class EventListAdaper extends BaseAdapter implements StickyListHeadersAdapter {

    private final Context ctx;
    private final LayoutInflater inflater;
    private final boolean isSlovenianLocale;
    private List<PrometEvent> data;

    public EventListAdaper(Context ctx) {
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.ctx = ctx;

        isSlovenianLocale = LocaleUtil.isSlovenianLocale(ctx);
        data = new ArrayList<>();
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).id;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            v = inflater.inflate(R.layout.item_event, parent, false);
            v.setTag(new EventItemHolder(v));
        }

        EventItemHolder holder = (EventItemHolder) v.getTag();
        final PrometEvent event = data.get(position);

        SpannableString titleText = new SpannableString(isSlovenianLocale ? event.causeSl : event.causeEn);
        SpannableString locationText = new SpannableString(isSlovenianLocale ? event.roadNameSl : event.roadNameEn);
        if (event.isHighPriority()) {
            int color = ContextCompat.getColor(ctx, android.R.color.holo_red_dark);
            titleText.setSpan(new ForegroundColorSpan(color), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            locationText.setSpan(new ForegroundColorSpan(color), 0, locationText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        holder.titleView.setText(titleText);
        holder.descriptionView.setText(isSlovenianLocale ? event.descriptionSl : event.descriptionEn);
        holder.locationView.setText(locationText);
        holder.timeView.setVisibility(View.INVISIBLE);
        /*holder.timeView.setVisibility(event.updated == null ? View.INVISIBLE : View.VISIBLE);
        holder.timeView.setText(event.updated == null ? "" : FuzzyDateTimeFormatter.getTimeAgo(ctx, event.updated.toLocalDateTime())); */
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new Events.ShowPointOnMap(new LatLng(event.lat, event.lng)));
            }
        });
        return v;
    }

    public void setData(List<PrometEvent> prometEvents) {
        this.data = prometEvents;
        Collections.sort(data, new Comparator<PrometEvent>() {
            @Override
            public int compare(PrometEvent lhs, PrometEvent rhs) {
                if (!rhs.isRoadworks() && lhs.isRoadworks())
                    return 1;

                if (!lhs.isRoadworks() && rhs.isRoadworks())
                    return -1;

                if (lhs.eventGroup != rhs.eventGroup) {
                    return lhs.eventGroup.ordinal() - rhs.eventGroup.ordinal();
                }

                return rhs.updated.compareTo(lhs.updated);
            }
        });

        notifyDataSetChanged();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = inflater.inflate(R.layout.item_list_header, parent, false);

        TextView textView = (TextView) v.findViewById(R.id.item_event_header);
        EventGroup type = data.get(position).eventGroup;

        // Avtoceste and hitre ceste are merged
        if (type == EventGroup.HITRA_CESTA) type = EventGroup.AVTOCESTA;
        if (data.get(position).isRoadworks()) {
            textView.setText(R.string.roadworks);
            return v;
        }

        String[] strings = ctx.getResources().getStringArray(R.array.road_type_strings);
        textView.setText(type == null ? strings[strings.length - 1] : strings[type.ordinal()]);
        return v;
    }

    @Override
    public long getHeaderId(int position) {
        EventGroup type = data.get(position).eventGroup;
        if (type == EventGroup.HITRA_CESTA)   // Join this with highways
            return EventGroup.AVTOCESTA.ordinal();

        if (data.get(position).isRoadworks())
            return 101;

        return type == null ? 100 : type.ordinal();
    }

    public int getItemPosition(long id) {

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).id == id)
                return i;
        }

        return 0;
    }

    private static class EventItemHolder {
        public CardView card;
        public TextView titleView;
        public TextView descriptionView;
        public TextView timeView;
        public TextView locationView;

        public EventItemHolder(View view) {
            this.titleView = (TextView) view.findViewById(R.id.item_event_title);
            this.descriptionView = (TextView) view.findViewById(R.id.item_event_description);
            this.timeView = (TextView)view.findViewById(R.id.item_event_time);
            this.locationView = (TextView)view.findViewById(R.id.item_event_location);
        }
    }

}
