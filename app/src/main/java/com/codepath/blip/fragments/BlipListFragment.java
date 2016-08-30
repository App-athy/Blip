package com.codepath.blip.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.blip.Adapters.BlipAdapter;
import com.codepath.blip.BlipApplication;
import com.codepath.blip.R;
import com.codepath.blip.clients.BackendClient;
import com.codepath.blip.models.Blip;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class BlipListFragment extends Fragment{

    @Inject BackendClient mBackendClient;

    private ArrayList<Blip> mBlips;
    private BlipAdapter mAdapter;
    protected RecyclerView rvBlips;
    protected LinearLayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_blip_list, container, false);
        rvBlips = (RecyclerView) v.findViewById(R.id.rvTweets);
        rvBlips.setAdapter(mAdapter);
        rvBlips.setLayoutManager(layoutManager);
        Bundle b = getActivity().getIntent().getParcelableExtra("bundle");

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BlipApplication) getActivity().getApplication()).getAppComponent().inject(this);

        mBlips = new ArrayList<>();
        mAdapter = new BlipAdapter(getActivity(), mBlips);
        layoutManager = new LinearLayoutManager(getActivity());
        populateBlips(25);
    }

    public void addAll(List<Blip> b) {
        mBlips.clear();
        mBlips.addAll(b);
        mAdapter.notifyDataSetChanged();
    }

    private void populateBlips(int numberOfBlips) {
        Observable<List<Blip>> blipListObservable = mBackendClient.getNearbyBlipsSubject();
        blipListObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Blip>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<Blip> blips) {
                        addAll(blips);
                    }
                });
    }
}
