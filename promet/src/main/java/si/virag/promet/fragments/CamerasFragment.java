package si.virag.promet.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import si.virag.promet.PrometApplication;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometCamera;

public class CamerasFragment extends Fragment {

    private static final String LOG_TAG = "Promet.CameraList";


    @Inject PrometApi prometApi;

    @Nullable
    private Subscription loadSubscription;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrometApplication application = (PrometApplication)getActivity().getApplication();
        application.component().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCameras();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (loadSubscription != null) {
            loadSubscription.unsubscribe();
            loadSubscription = null;
        }
    }

    private void loadCameras() {
        loadSubscription = prometApi.getPrometCameras()
             .subscribe(new Subscriber<List<PrometCamera>>() {
                 @Override
                 public void onCompleted() {
                    loadSubscription = null;
                 }

                 @Override
                 public void onError(Throwable e) {

                 }

                 @Override
                 public void onNext(List<PrometCamera> prometCameras) {
                    Log.d(LOG_TAG, "Cameras: " + prometCameras);
                 }
             });
    }
}
