package si.virag.promet.fragments;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dagger.android.support.DaggerFragment;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.FlexibleItemDecoration;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import si.virag.promet.R;
import si.virag.promet.api.data.PrometApi;
import si.virag.promet.api.model.PrometCamera;
import si.virag.promet.api.model.TrafficInfo;
import si.virag.promet.fragments.cameras.CameraHeaderItem;
import si.virag.promet.fragments.cameras.CameraItem;

public final class CamerasFragment extends DaggerFragment implements SwipeRefreshLayout.OnRefreshListener {

    @Inject
    PrometApi prometApi;

    private TextView emptyView;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView list;

    @Nullable
    private Subscription loadSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cameras, container, false);
        emptyView = v.findViewById(R.id.cameras_empty);
        refreshLayout = v.findViewById(R.id.cameras_refresh);
        list = v.findViewById(R.id.cameras_list);

        list.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        list.addItemDecoration(new FlexibleItemDecoration(requireActivity()).withDefaultDivider());
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
    public void onStop() {
        super.onStop();
        if (loadSubscription != null) {
            loadSubscription.unsubscribe();
            loadSubscription = null;
        }
    }

    private void loadCameras() {
        emptyView.setText(R.string.loading);
        prometApi.getTrafficInfo()
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new Subscriber<TrafficInfo>() {
                     @Override
                     public void onCompleted() {
                        refreshLayout.setRefreshing(false);
                     }

                     @Override
                     public void onError(Throwable e) {
                         emptyView.setText(R.string.load_error);
                         emptyView.setVisibility(View.VISIBLE);
                         list.setVisibility(View.INVISIBLE);
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
                         emptyView.setVisibility(View.INVISIBLE);
                         list.setVisibility(View.VISIBLE);
                         showCameras(trafficInfo.cameras);
                     }
                 });
    }

    private void showCameras(List<PrometCamera> cameras) {
        List<CameraHeaderItem> items = new ArrayList<>();
        Map<String, CameraHeaderItem> headers = new HashMap<>();

        // Generate sectioned items
        for (PrometCamera camera : cameras) {
            CameraHeaderItem header = headers.get(camera.getRegion());
            if (header == null) {
                header = new CameraHeaderItem(camera.getRegion());
                headers.put(camera.getRegion(), header);
                items.add(header);
            }

            header.addSubItem(new CameraItem(header, camera));
        }

        Collections.sort(items, (item1, item2) -> item1.title.compareTo(item2.title));

        if (list.getAdapter() == null) {
            final FlexibleAdapter<CameraHeaderItem> adapter = new FlexibleAdapter<>(items);
            list.setAdapter(adapter);
        } else {
            final FlexibleAdapter<CameraHeaderItem> adapter = (FlexibleAdapter<CameraHeaderItem>) list.getAdapter();
            for (CameraHeaderItem header : adapter.getExpandedItems()) {
                CameraHeaderItem item = headers.get(header.title);
                if (item != null) item.setExpanded(true);
            }

            adapter.updateDataSet(items);
        }
    }

    @Override
    public void onRefresh() {
        loadCameras();
    }
}
