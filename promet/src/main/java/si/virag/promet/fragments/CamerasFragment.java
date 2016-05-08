package si.virag.promet.fragments;


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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.fragments.ui.CameraCategory;
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
        adapter = new CamerasAdapter(getContext(), new ArrayList<CameraCategory>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cameras, container, false);
        ButterKnife.inject(this, v);

        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2, R.color.refresh_color_3, R.color.refresh_color_4);
        return v;
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
    
    }

    @Override
    public void onRefresh() {
        loadCameras();
    }
}
