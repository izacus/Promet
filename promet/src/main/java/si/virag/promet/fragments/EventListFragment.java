package si.virag.promet.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import si.virag.promet.Events;
import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.fragments.ui.EventListAdaper;
import si.virag.promet.fragments.ui.EventListFilter;
import si.virag.promet.utils.PrometSettings;

public class EventListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = "Promet.EventList";
    private EventListAdaper adapter;

    @Inject protected PrometApi prometApi;
    @Inject protected PrometSettings prometSettings;

    @InjectView(R.id.events_list) protected StickyListHeadersListView list;
    @InjectView(R.id.events_refresh) protected SwipeRefreshLayout refreshLayout;
    @InjectView(R.id.events_empty) protected TextView emptyView;

    private TextView headerView;

    @Nullable
    private Subscription loadSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EventListAdaper(getActivity());
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
        list.setEmptyView(emptyView);
        list.addHeaderView(headerViewContainer);
        list.setAdapter(adapter);

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

        Crouton.clearCroutonsForActivity(getActivity());
        loadSubscription = events.subscribeOn(Schedulers.io())
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .flatMap(new Func1<List<PrometEvent>, Observable<PrometEvent>>() {
                                      @Override
                                      public Observable<PrometEvent> call(List<PrometEvent> prometEvents) {
                                          return Observable.from(prometEvents);
                                      }
                                  })
                                  .filter(new EventListFilter(prometSettings))
                                  .toList()
                                  .subscribe(new Subscriber<List<PrometEvent>>() {
                                      @Override
                                      public void onNext(List<PrometEvent> prometEvents) {
                                          updateHeaderView();
                                          adapter.setData(prometEvents);

                                          // We might have a scroll event pending, execute it now.
                                          Events.ShowEventInList eventInList = EventBus.getDefault().getStickyEvent(Events.ShowEventInList.class);
                                          if (eventInList != null) {
                                              int position = adapter.getItemPosition(eventInList.id);
                                              list.smoothScrollToPosition(position + 1);
                                              EventBus.getDefault().removeStickyEvent(eventInList);
                                          }

                                          if (force)
                                              EventBus.getDefault().post(new Events.UpdateMap());
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
                                          emptyView.setText("Podatkov ni bilo mogoče naložiti.");
                                          Crouton.makeText(getActivity(), "Podatkov ni bilo mogoče naložiti.", Style.ALERT).show();
                                      }
                                  });
    }

    private void updateHeaderView() {
        if (prometSettings.getShowAvtoceste() &&
            prometSettings.getShowLokalneCeste() &&
            prometSettings.getShowBorderCrossings() &&
            prometSettings.getShowRegionalneCeste()) {

            headerView.setVisibility(View.GONE);
            return;
        }

        headerView.setVisibility(View.VISIBLE);
        String check = "\u2713";
        String cross = "\u2717";

        String text = getString(R.string.list_hint,
                                prometSettings.getShowAvtoceste() ? check : cross,
                                prometSettings.getShowBorderCrossings() ? check : cross,
                                prometSettings.getShowRegionalneCeste() ? check : cross,
                                prometSettings.getShowLokalneCeste() ? check : cross);
        headerView.setText(text);
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

    public void onEventMainThread(final Events.ShowEventInList e) {
        int position = adapter.getItemPosition(e.id);
        list.smoothScrollToPosition(position + 1);

        if (adapter.getCount() > 0)
            EventBus.getDefault().removeStickyEvent(e);
    }

    public void onEventMainThread(Events.UpdateEventList e) {
        loadEvents(false);
    }
}
