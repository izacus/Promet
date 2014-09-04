package si.virag.promet.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.map.PrometMaps;
import si.virag.promet.utils.SubscriberAdapter;

import javax.inject.Inject;
import java.util.List;

public class MapFragment extends Fragment {

    private static final String LOG_TAG = "Promet.MapFragment";

    // Views
    @InjectView(R.id.map_map) protected MapView mapView;

    // Module dependencies
    @Inject PrometApi prometApi;
    @Inject PrometMaps prometMaps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dagger injection
        ((PrometApplication)getActivity().getApplication()).inject(this);
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
        prometApi.getPrometEvents()
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new SubscriberAdapter<List<PrometEvent>>() {

                     @Override
                     public void onError(Throwable throwable) {
                         super.onError(throwable);

                         // TODO
                     }

                     @Override
                     public void onNext(List<PrometEvent> prometEvents) {
                         super.onNext(prometEvents);
                         prometMaps.showEvents(prometEvents);
                     }

                 });
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        prometMaps.setMapInstance(mapView.getMap());
        displayTrafficData();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
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
}
