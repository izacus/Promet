package si.virag.promet.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.common.collect.ImmutableList;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import si.virag.promet.MainActivity;
import si.virag.promet.PrometApplication;
import si.virag.promet.R;
import si.virag.promet.api.PrometApi;
import si.virag.promet.api.model.PrometEvent;
import si.virag.promet.fragments.ui.EventListAdaper;
import si.virag.promet.utils.SubscriberAdapter;

import javax.inject.Inject;

public class EventListFragment extends Fragment {

    private EventListAdaper adapter;

    @Inject protected PrometApi prometApi;

    @InjectView(R.id.events_list) protected StickyListHeadersListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EventListAdaper(getActivity());
        ((PrometApplication) getActivity().getApplication()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.inject(this, v);

        SystemBarTintManager manager = ((MainActivity)getActivity()).getTintManager();
        list.setPadding(list.getPaddingTop(), list.getPaddingLeft(), list.getPaddingRight(), list.getPaddingBottom() + manager.getConfig().getPixelInsetBottom());
        list.setAdapter(adapter);
        return v;
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
