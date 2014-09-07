package si.virag.promet.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.ImmutableList;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import si.virag.promet.Events;
import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.map.PrometMaps;
import si.virag.promet.utils.PrometSettings;
import si.virag.promet.utils.SubscriberAdapter;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    private static final String LOG_TAG = "Promet.MapFragment";

    // Views
    @InjectView(R.id.map_map) protected MapView mapView;

    // Module dependencies
    @Inject PrometApi prometApi;
    @Inject PrometMaps prometMaps;
    @Inject PrometSettings prometSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
        Crouton.clearCroutonsForActivity(getActivity());
        EventBus.getDefault().post(new Events.RefreshStarted());

        prometApi.getPrometEvents()
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .flatMap(new Func1<List<PrometEvent>, Observable<PrometEvent>>() {
                     @Override
                     public Observable<PrometEvent> call(List<PrometEvent> prometEvents) {
                         return Observable.from(prometEvents);
                     }
                 })
                 .filter(new Func1<PrometEvent, Boolean>() {
                     @Override
                     public Boolean call(PrometEvent prometEvent) {

                         if (prometEvent.roadType == null)
                             return true;

                         switch (prometEvent.roadType) {
                             case AVTOCESTA:
                             case HITRA_CESTA:
                                 return prometSettings.getShowAvtoceste();
                             case REGIONALNA_CESTA:
                                 return prometSettings.getShowRegionalneCeste();
                             case LOKALNA_CESTA:
                                 return prometSettings.getShowLokalneCeste();

                             default:
                                 return true;
                         }
                     }
                 })
                 .toList()
                 .subscribe(new Subscriber<List<PrometEvent>>() {

                     @Override
                     public void onCompleted() {
                         EventBus.getDefault().post(new Events.RefreshCompleted());
                     }

                     @Override
                     public void onError(Throwable throwable) {
                         Log.d(LOG_TAG, "Error when loading!", throwable);
                         EventBus.getDefault().post(new Events.RefreshCompleted());
                         Crouton.makeText(getActivity(), "Podatkov ni bilo mogoče naložiti.", Style.ALERT).show();
                     }

                     @Override
                     public void onNext(List<PrometEvent> prometEvents) {
                         prometMaps.showEvents(prometEvents);
                     }
                 });
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        GoogleMap map = mapView.getMap();
        // Fix padding for devices with transparent navigation bar
        if (map != null)
            map.setPadding(0, 0, 0, ((MainActivity)getActivity()).getTintManager().getConfig().getPixelInsetBottom());

        prometMaps.setMapInstance(map);
        displayTrafficData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_map, menu);

        menu.findItem(R.id.menu_map_avtoceste).setChecked(prometSettings.getShowAvtoceste());
        menu.findItem(R.id.menu_map_lokalne_ceste).setChecked(prometSettings.getShowLokalneCeste());
        menu.findItem(R.id.menu_map_regionalne_ceste).setChecked(prometSettings.getShowRegionalneCeste());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (!item.isCheckable())
            return false;

        boolean enabled = !item.isChecked();
        item.setChecked(enabled);

        switch (item.getItemId()) {

            case R.id.menu_map_avtoceste:
                prometSettings.setShowAvtoceste(enabled);
                break;

            case R.id.menu_map_regionalne_ceste:
                prometSettings.setShowRegionalneCeste(enabled);
                break;

            case R.id.menu_map_lokalne_ceste:
                prometSettings.setShowLokalneCeste(enabled);
                break;

            default:
                return false;
        }

        displayTrafficData();
        return true;
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
