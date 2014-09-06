package si.virag.promet.fragments.ui;


import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.google.common.collect.ImmutableList;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import si.virag.fuzzydatetime.FuzzyDateTimeFormatter;
import si.virag.promet.R;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.RoadType;

public class EventListAdaper extends BaseAdapter implements StickyListHeadersAdapter {

    private final Context ctx;
    private final LayoutInflater inflater;
    private ImmutableList<PrometEvent> data;

    public EventListAdaper(Context ctx) {
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.ctx = ctx;
        data = ImmutableList.<PrometEvent>builder().build();
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
        holder.titleView.setText(event.cause);
        holder.descriptionView.setText(event.description);
        holder.timeView.setVisibility(event.entered == null ? View.INVISIBLE : View.VISIBLE);
        holder.timeView.setText(event.entered == null ? "" : FuzzyDateTimeFormatter.getTimeAgo(ctx, event.entered));
        return v;
    }

    public void setData(ImmutableList<PrometEvent> prometEvents) {
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
        String[] strings = ctx.getResources().getStringArray(R.array.road_type_strings);
        textView.setText(type == null ? strings[strings.length - 1] : strings[type.ordinal()]);
        return v;
    }

    @Override
    public long getHeaderId(int position) {
        RoadType type = data.get(position).roadType;
        return type == null ? 100 : type.ordinal();
    }

    private static class EventItemHolder {
        public TextView titleView;
        public TextView descriptionView;
        public TextView timeView;

        public EventItemHolder(View view) {
            this.titleView = (TextView) view.findViewById(R.id.item_event_title);
            this.descriptionView = (TextView) view.findViewById(R.id.item_event_description);
            this.timeView = (TextView)view.findViewById(R.id.item_event_time);
        }
    }

}
