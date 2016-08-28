package com.codepath.blip.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.blip.BlipApplication;
import com.codepath.blip.EndlessRecyclerViewScrollListener;
import com.codepath.blip.clients.BackendClient;

import javax.inject.Inject;

public class UserBlipFragment extends BlipListFragment {

    @Inject BackendClient mBackendClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateBlips(25);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = super.onCreateView(inflater, container, savedInstanceState);

        rvBlips.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                populateBlips(totalItemsCount + 25);
            }
        });

        return v;
    }

    private void populateBlips(int numberOfBlips) {
        int x = 3;
    }
}
