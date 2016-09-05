package com.codepath.blip.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.blip.BlipApplication;
import com.codepath.blip.clients.BackendClient;
import com.codepath.blip.models.Blip;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class UserBlipFragment extends BlipListFragment {

    @Inject BackendClient mBackendClient;


    public UserBlipFragment() { }

    public static UserBlipFragment newInstance() {
        UserBlipFragment frag = new UserBlipFragment();
        return frag;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BlipApplication) getActivity().getApplication()).getAppComponent().inject(this);

        populateBlips();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = super.onCreateView(inflater, container, savedInstanceState);

        return v;
    }

    private void populateBlips() {
        Observable<List<Blip>> blipListObservable = mBackendClient.getBlipsForUser();
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
                        addBlips(blips);
                    }
                });
    }

    private void addBlips(List<Blip> blips) {
        super.addAll(blips);
    }
}
