package com.codepath.blip.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.blip.Adapters.BlipAdapter;
import com.codepath.blip.R;
import com.codepath.blip.models.Blip;

import java.util.ArrayList;
import java.util.List;

public class BlipListFragment extends Fragment{
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

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    }
}
