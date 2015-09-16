package si.virag.promet.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.nispok.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import org.joda.time.DateTime;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import si.virag.promet.Events;
import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometCounter;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.TrafficStatus;
import si.virag.promet.fragments.ui.EventListFilter;
import si.virag.promet.map.PrometMaps;
import si.virag.promet.utils.PrometSettings;

public class MapFragment extends Fragment {

    private static final String LOG_TAG = "Promet.MapFragment";

    // Views
    @InjectView(R.id.map_map) protected MapView mapView;

    // Module dependencies
    @Inject PrometApi prometApi;
    @Inject PrometMaps prometMaps;
    @Inject PrometSettings prometSettings;

    @Nullable
    private Subscription loadSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dagger injection
        PrometApplication application = (PrometApplication) getActivity().getApplication();
        application.component().inject(this);
        MapsInitializer.initialize(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.inject(this, view);
        mapView.onCreate(savedInstanceState);
        return view;
    }

    private void displayTrafficData() {
        EventBus.getDefault().post(new Events.RefreshStarted());

        Observable<List<PrometEvent>> events = prometApi.getPrometEvents()
                .flatMap(new Func1<List<PrometEvent>, Observable<PrometEvent>>() {
                    @Override
                    public Observable<PrometEvent> call(List<PrometEvent> prometEvents) {
                        return Observable.from(prometEvents);
                    }
                })
                .filter(new EventListFilter(prometSettings))
                .toList();

        Observable<List<PrometCounter>> counters = prometApi.getPrometCounters()
                                                   .onErrorReturn(new Func1<Throwable, List<PrometCounter>>() {
                                                       @Override
                                                       public List<PrometCounter> call(Throwable throwable) {
                                                           Log.e(LOG_TAG, "Failed to load traffic counters!", throwable);
                                                           return new ArrayList<>();
                                                       }
                                                   })
                                                   .flatMap(new Func1<List<PrometCounter>, Observable<PrometCounter>>() {
                                                       @Override
                                                       public Observable<PrometCounter> call(List<PrometCounter> prometCounters) {
                                                           return Observable.from(prometCounters);
                                                       }
                                                   })
                                                   .filter(new Func1<PrometCounter, Boolean>() {
                                                       @Override
                                                       public Boolean call(PrometCounter prometCounter) {
                                                           return prometCounter.status != TrafficStatus.NORMAL_TRAFFIC && prometCounter.status != TrafficStatus.NO_DATA;
                                                       }
                                                   }).toList();

        loadSubscription = events.zipWith(counters, new Func2<List<PrometEvent>, List<PrometCounter>, Pair<List<PrometEvent>, List<PrometCounter>>>() {
            @Override
            public Pair<List<PrometEvent>, List<PrometCounter>> call(List<PrometEvent> prometEvents, List<PrometCounter> prometCounters) {
                return new Pair<>(prometEvents, prometCounters);
            }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Pair<List<PrometEvent>, List<PrometCounter>>>() {
            @Override
            public void onCompleted() {
                loadSubscription = null;
            }

            @Override
            public void onError(Throwable e) {
                Log.d(LOG_TAG, "Error when loading!", e);
                EventBus.getDefault().post(new Events.RefreshCompleted());
                Activity activity = getActivity();
                if (activity != null) {
                    Snackbar.with(getActivity().getApplicationContext())
                            .text(R.string.load_error)
                            .textTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
                            .color(Color.RED)
                            .show(activity);
                }
            }

            @Override
            public void onNext(Pair<List<PrometEvent>, List<PrometCounter>> eventPair) {
                Log.d(LOG_TAG, eventPair.first.toString());

                DateTime lastUpdateTime = null;
/*                if (eventPair.second.size() > 0) {
                    int mostRecentIdx = eventPair.second.indexOf(Collections.min(eventPair.second, new Comparator<PrometCounter>() {
                        @Override
                        public int compare(PrometCounter lhs, PrometCounter rhs) {
                            return -lhs.updated.compareTo(rhs.updated);
                        }
                    }));
                     lastUpdateTime = eventPair.second.get(mostRecentIdx).updated;
                } */

                EventBus.getDefault().post(new Events.RefreshCompleted(lastUpdateTime));
                prometMaps.showEvents(getActivity(), eventPair.first, eventPair.second);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                if (!isAdded()) return;

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    map.setPadding(0, 0, ((MainActivity) getActivity()).getTintManager().getConfig().getPixelInsetRight(), 0);
                }
                else {
                    map.setPadding(0, 0, 0, ((MainActivity) getActivity()).getTintManager().getConfig().getPixelInsetBottom());
                }

                prometMaps.setMapInstance(getActivity(), map);
                displayTrafficData();
            }
        });

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

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (loadSubscription != null) {
            loadSubscription.unsubscribe();
            loadSubscription = null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    public void onEventMainThread(Events.ShowPointOnMap e) {
        prometMaps.showPoint(e.point);
    }
    public void onEventMainThread(Events.UpdateMap e) {
        displayTrafficData();
    }

}
