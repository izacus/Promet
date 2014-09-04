package si.virag.promet.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.google.common.collect.ImmutableList;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.fragments.ui.EventListAdaper;
import si.virag.promet.utils.SubscriberAdapter;

import javax.inject.Inject;

public class EventListFragment extends ListFragment {

    private EventListAdaper adapter;

    @Inject protected PrometApi prometApi;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EventListAdaper(getActivity());
        setListAdapter(adapter);

        ((PrometApplication) getActivity().getApplication()).inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int padding = (int)getResources().getDimension(R.dimen.listview_padding);
        getListView().setPadding(padding, padding, padding, padding);
        getListView().setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
    }

    @Override
    public void onResume() {
        super.onResume();

        prometApi.getPrometEvents()
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new SubscriberAdapter<ImmutableList<PrometEvent>>() {
                     @Override
                     public void onNext(ImmutableList<PrometEvent> prometEvents) {
                         super.onNext(prometEvents);
                         adapter.setData(prometEvents);
                     }

                     @Override
                     public void onError(Throwable throwable) {
                         super.onError(throwable);
                     }
                 });
    }
}
