package com.codepath.blip.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.blip.clients.BackendClient;
import com.codepath.blip.models.Blip;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class UserBlipFragment extends BlipListFragment {

    @Inject BackendClient mBackendClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
