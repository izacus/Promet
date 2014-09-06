package si.virag.promet.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.ImmutableList;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import si.virag.promet.Events;
import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.fragments.ui.EventListAdaper;
import si.virag.promet.utils.SubscriberAdapter;

import javax.inject.Inject;
import java.util.List;

public class EventListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    private EventListAdaper adapter;

    @Inject protected PrometApi prometApi;

    @InjectView(R.id.events_list) protected StickyListHeadersListView list;
    @InjectView(R.id.events_refresh) protected SwipeRefreshLayout refreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EventListAdaper(getActivity());
        ((PrometApplication) getActivity().getApplication()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.inject(this, v);

        SystemBarTintManager manager = ((MainActivity)getActivity()).getTintManager();
        list.setPadding(list.getPaddingTop(), list.getPaddingLeft(), list.getPaddingRight(), list.getPaddingBottom() + manager.getConfig().getPixelInsetBottom());
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2, R.color.refresh_color_3, R.color.refresh_color_4);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents(false);
    }

    private void loadEvents(boolean force)
    {
        Observable<List<PrometEvent>> events;
        if (force)
            events = prometApi.getReloadPrometEvents();
        else
            events = prometApi.getPrometEvents();

        events.subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Subscriber<List<PrometEvent>>() {
                  @Override
                  public void onNext(List<PrometEvent> prometEvents) {
                      adapter.setData(prometEvents);
                  }

                  @Override
                  public void onCompleted() {
                      refreshLayout.setRefreshing(false);
                  }

                  @Override
                  public void onError(Throwable throwable) {
                      refreshLayout.setRefreshing(false);
                      // TODO
                  }


              });
    }

    @Override
    public void onRefresh() {
        loadEvents(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // On item click we must focus the map on the event in previous fragment
        PrometEvent event = (PrometEvent) adapter.getItem(position);
        EventBus.getDefault().post(new Events.ShowPointOnMap(new LatLng(event.lat, event.lng)));
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(Events.ShowEventInList e) {
        int position = adapter.getItemPosition(e.id);
        list.smoothScrollToPosition(position);
    }
}