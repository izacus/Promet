package si.virag.promet.fragments.ui;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.google.common.collect.ImmutableList;
import si.virag.promet.R;
import si.virag.promet.api.model.PrometEvent;

public class EventListAdaper extends BaseAdapter {

    private LayoutInflater inflater;
    private ImmutableList<PrometEvent> data;

    public EventListAdaper(Context ctx) {
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        holder.titleView.setText(data.get(position).cause);
        return v;
    }

    public void setData(ImmutableList<PrometEvent> prometEvents) {
        this.data = prometEvents;
        notifyDataSetChanged();
    }

    private static class EventItemHolder {
        public TextView titleView;

        public EventItemHolder(View view) {
            this.titleView = (TextView) view.findViewById(R.id.item_event_title);
        }
    }

}
