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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nispok.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.fragments.ui.CamerasAdapter;

public class CamerasFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = "Promet.CameraList";

    @Inject PrometApi prometApi;

    @InjectView(R.id.cameras_empty) public View emptyView;
    @InjectView(R.id.cameras_refresh) public SwipeRefreshLayout refreshLayout;
    @InjectView(R.id.cameras_list) public RecyclerView list;

    @Nullable
    private Subscription loadSubscription;
    private CamerasAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrometApplication application = (PrometApplication)getActivity().getApplication();
        application.component().inject(this);
        adapter = new CamerasAdapter(new ArrayList<PrometCamera>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cameras, container, false);
        ButterKnife.inject(this, v);

        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        list.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2, R.color.refresh_color_3, R.color.refresh_color_4);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadCameras();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (loadSubscription != null) {
            loadSubscription.unsubscribe();
            loadSubscription = null;
        }
    }

    private void loadCameras() {
        prometApi.getPrometCameras()
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new Subscriber<List<PrometCamera>>() {
                     @Override
                     public void onCompleted() {
                        refreshLayout.setRefreshing(false);
                     }

                     @Override
                     public void onError(Throwable e) {
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
                     public void onNext(List<PrometCamera> prometCameras) {
                        adapter.setData(prometCameras);
                     }
                 });
    }

    @Override
    public void onRefresh() {
        loadCameras();
    }
}
