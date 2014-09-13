package si.virag.promet.fragments.ui;


import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import si.virag.fuzzydatetime.FuzzyDateTimeFormatter;
import si.virag.promet.R;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.RoadType;

import java.util.ArrayList;
import java.util.List;

public class EventListAdaper extends BaseAdapter implements StickyListHeadersAdapter {

    private final Context ctx;
    private final LayoutInflater inflater;
    private List<PrometEvent> data;

    public EventListAdaper(Context ctx) {
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.ctx = ctx;
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
        PrometEvent event = data.get(position);

        SpannableString titleText = new SpannableString(event.cause);
        SpannableString locationText = new SpannableString(event.roadName);
        if (event.isHighPriority()) {
            int color = ctx.getResources().getColor(android.R.color.holo_red_dark);
            titleText.setSpan(new ForegroundColorSpan(color), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            locationText.setSpan(new ForegroundColorSpan(color), 0, locationText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        holder.titleView.setText(titleText);
        holder.descriptionView.setText(event.description);
        holder.locationView.setText(locationText);
        holder.timeView.setVisibility(event.entered == null ? View.INVISIBLE : View.VISIBLE);
        holder.timeView.setText(event.entered == null ? "" : FuzzyDateTimeFormatter.getTimeAgo(ctx, event.entered));
        return v;
    }

    public void setData(List<PrometEvent> prometEvents) {
        this.data = prometEvents;
        notifyDataSetChanged();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = inflater.inflate(R.layout.item_list_header, parent, false);

        TextView textView = (TextView) v.findViewById(R.id.item_event_header);
        RoadType type = data.get(position).roadType;

        // Avtoceste and hitre ceste are merged
        if (type == RoadType.HITRA_CESTA) type = RoadType.AVTOCESTA;

        String[] strings = ctx.getResources().getStringArray(R.array.road_type_strings);
        textView.setText(type == null ? strings[strings.length - 1] : strings[type.ordinal()]);
        return v;
    }

    @Override
    public long getHeaderId(int position) {
        RoadType type = data.get(position).roadType;
        if (type == RoadType.HITRA_CESTA)   // Join this with highways
            return RoadType.AVTOCESTA.ordinal();

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
