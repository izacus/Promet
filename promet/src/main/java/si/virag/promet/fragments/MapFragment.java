package si.virag.promet.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.nispok.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import si.virag.promet.Events;
import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.data.PrometApi;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.api.model.TrafficInfo;
import si.virag.promet.fragments.ui.EventListFilter;
import si.virag.promet.map.PrometMaps;
import si.virag.promet.utils.PrometSettings;

public class MapFragment extends DaggerFragment {

    private static final String LOG_TAG = "Promet.MapFragment";

    // Views
    @BindView(R.id.map_map) protected MapView mapView;

    // Module dependencies
    @Inject
    PrometApi prometApi;
    @Inject PrometMaps prometMaps;
    @Inject PrometSettings prometSettings;

    @Nullable
    private Subscription loadSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        mapView.onCreate(savedInstanceState);
        return view;
    }

    private void loadTrafficData() {
        EventBus.getDefault().post(new Events.RefreshStarted());
        loadSubscription = prometApi.getTrafficInfo()
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new Subscriber<TrafficInfo>() {
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
                     public void onNext(TrafficInfo trafficInfo) {
                         EventBus.getDefault().post(new Events.RefreshCompleted(null));
                         displayTrafficData(trafficInfo);
                     }
                 });
    }

    private void displayTrafficData(@NonNull TrafficInfo info) {
        List<PrometEvent> prometEvents = Observable.from(info.events)
                                         .filter(new EventListFilter(prometSettings))
                                         .toList()
                                         .toBlocking().single();

        List<PrometCamera> cameras = prometSettings.getShowCameras() ? info.cameras : Collections.<PrometCamera>emptyList();
        prometMaps.showData(getActivity(), prometEvents, cameras);
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
                loadTrafficData();
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
        if (mapView != null) mapView.onPause();

        if (loadSubscription != null) {
            loadSubscription.unsubscribe();
            loadSubscription = null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    public void onEventMainThread(Events.ShowPointOnMap e) {
        prometMaps.showPoint(e.point);
    }
    public void onEventMainThread(Events.UpdateMap e) {
        loadTrafficData();
    }


    private static class DataTriple {
        public final List<PrometEvent> events;
        public final List<PrometCamera> cameras;

        public DataTriple(List<PrometEvent> events, List<PrometCamera> cameras) {
            this.cameras = cameras;
            this.events = events;
        }

        @Override
        public String toString() {
            return "DataTriple{" +
                    "events=" + events +
                    ", cameras=" + cameras +
                    '}';
        }
    }
}
