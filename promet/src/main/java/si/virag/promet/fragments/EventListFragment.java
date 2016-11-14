package si.virag.promet.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.DividerItemDecoration;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import si.virag.promet.Events;
import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.EventGroup;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.fragments.events.EventHeaderItem;
import si.virag.promet.fragments.events.EventItem;
import si.virag.promet.fragments.ui.EventListFilter;
import si.virag.promet.utils.PrometSettings;

public class EventListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = "Promet.EventList";

    @Inject protected PrometApi prometApi;
    @Inject protected PrometSettings prometSettings;

    @InjectView(R.id.events_list) protected RecyclerView list;
    @InjectView(R.id.events_refresh) protected SwipeRefreshLayout refreshLayout;
    @InjectView(R.id.events_empty) protected TextView emptyView;

    private TextView headerView;

    @Nullable
    private Subscription loadSubscription;

    @Nullable
    private FlexibleAdapter<EventItem> adapter;

    @Nullable
    private List<EventItem> adapterItems;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrometApplication application = (PrometApplication) getActivity().getApplication();
        application.component().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.inject(this, v);

        LinearLayout headerViewContainer = new LinearLayout(getActivity());
        headerView = new TextView(getActivity());
        headerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        headerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        headerView.setGravity(Gravity.CENTER);
        headerView.setPadding(0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4.0f, getResources().getDisplayMetrics()), 0, 0);
        headerViewContainer.addView(headerView);

        SystemBarTintManager manager = ((MainActivity)getActivity()).getTintManager();
        list.setPadding(list.getPaddingTop(), list.getPaddingLeft(), list.getPaddingRight(), list.getPaddingBottom() + manager.getConfig().getPixelInsetBottom());
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        list.addItemDecoration(new DividerItemDecoration(getContext()));

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2, R.color.refresh_color_3, R.color.refresh_color_4);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (loadSubscription != null) {
            loadSubscription.unsubscribe();
            loadSubscription = null;
        }
    }

    private void loadEvents(final boolean force)
    {
        refreshLayout.setRefreshing(true);
        Observable<List<PrometEvent>> events;
        if (force)
            events = prometApi.getReloadPrometEvents();
        else
            events = prometApi.getPrometEvents();

        loadSubscription = events.subscribeOn(Schedulers.io())
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .flatMap(new Func1<List<PrometEvent>, Observable<PrometEvent>>() {
                                      @Override
                                      public Observable<PrometEvent> call(List<PrometEvent> prometEvents) {
                                          return Observable.from(prometEvents);
                                      }
                                  })
                                  .filter(new EventListFilter(prometSettings))
                                  .toSortedList(new Func2<PrometEvent, PrometEvent, Integer>() {
                                      @Override
                                      public Integer call(PrometEvent lhs, PrometEvent rhs) {
                                          if (!rhs.isRoadworks() && lhs.isRoadworks())
                                              return 1;

                                          if (!lhs.isRoadworks() && rhs.isRoadworks())
                                              return -1;

                                          if (lhs.eventGroup != rhs.eventGroup) {
                                              return lhs.eventGroup.ordinal() - rhs.eventGroup.ordinal();
                                          }

                                          return rhs.updated.compareTo(lhs.updated);
                                      }
                                  })
                                  .subscribe(new Subscriber<List<PrometEvent>>() {
                                      @Override
                                      public void onNext(List<PrometEvent> prometEvents) {
                                          prepareAndApplyData(prometEvents);
                                          EventBus.getDefault().post(new Events.UpdateActivityHeader());

                                          // We might have a scroll event pending, execute it now.
                                          Events.ShowEventInList eventInList = EventBus.getDefault().getStickyEvent(Events.ShowEventInList.class);
                                          if (eventInList != null) {
                                              scrollToEvent(eventInList.id);
                                              EventBus.getDefault().removeStickyEvent(eventInList);
                                          }

                                          if (force)
                                              EventBus.getDefault().post(new Events.UpdateMap());

                                          list.setVisibility(View.VISIBLE);
                                          emptyView.setVisibility(View.INVISIBLE);
                                      }

                                      @Override
                                      public void onCompleted() {
                                          refreshLayout.setRefreshing(false);
                                          loadSubscription = null;
                                      }

                                      @Override
                                      public void onError(Throwable throwable) {
                                          Log.e(LOG_TAG, "Error!", throwable);
                                          refreshLayout.setRefreshing(false);
                                          list.setVisibility(View.INVISIBLE);
                                          emptyView.setVisibility(View.VISIBLE);
                                          emptyView.setText(R.string.load_error);
                                          Activity activity = getActivity();
                                          if (activity != null) {
                                            Snackbar.with(getActivity().getApplicationContext())
                                                    .text(R.string.load_error)
                                                    .textTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
                                                    .color(Color.RED)
                                                    .show(activity);
                                          }
                                      }
                                  });
    }

    private void prepareAndApplyData(List<PrometEvent> prometEvents) {
        Map<String, EventHeaderItem> headerMap = new HashMap<>();
        List<EventItem> itemList = new ArrayList<>();
        String[] roadTypeStrings = getResources().getStringArray(R.array.road_type_strings);

        for (PrometEvent event : prometEvents) {
            String titleString;
            if (event.isRoadworks()) {
                titleString = getString(R.string.roadworks);
            } else {
                EventGroup type = event.eventGroup;
                // Avtoceste and hitre ceste are merged
                if (type == EventGroup.HITRA_CESTA) type = EventGroup.AVTOCESTA;
                titleString = type == null ? roadTypeStrings[roadTypeStrings.length - 1] : roadTypeStrings[type.ordinal()];
            }

            EventHeaderItem header;
            // We figured out the title, now prepare the header item
            if (headerMap.containsKey(titleString)) {
                header = headerMap.get(titleString);
            } else {
                header = new EventHeaderItem(titleString);
                headerMap.put(titleString, header);
            }

            EventItem item = new EventItem(header, event);
            itemList.add(item);
        }


        if (adapter == null) {
            adapter = new FlexibleAdapter<>(itemList);
            adapter.setDisplayHeadersAtStartUp(true);
            list.setAdapter(adapter);
        } else {
            adapter.updateDataSet(itemList);
        }

        adapterItems = itemList;
    }

    @Override
    public void onRefresh() {
        loadEvents(true);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private boolean scrollToEvent(long eventId) {
        if (adapterItems == null || adapter == null) return false;

        // Find item and then scroll to it.
        int position = -1;
        for (int i = 0; i < adapterItems.size(); i++) {
            //noinspection ConstantConditions
            if (!(adapterItems.get(i) instanceof EventItem)) continue;
            EventItem item = adapterItems.get(i);
            if (item.getEvent().id == eventId) {
                position = i;
                break;
            }
        }

        if (position < 0) return false;
        LinearLayoutManager manager = (LinearLayoutManager) list.getLayoutManager();
        manager.scrollToPositionWithOffset(position, 0);
        return true;
    }

    public void onEventMainThread(final Events.ShowEventInList e) {
        if (scrollToEvent(e.id)) {
            EventBus.getDefault().removeStickyEvent(e);
        }
    }

    public void onEventMainThread(Events.UpdateEventList e) {
        loadEvents(false);
    }
}
