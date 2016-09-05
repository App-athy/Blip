package com.codepath.blip.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.blip.Adapters.BlipAdapter;
import com.codepath.blip.R;
import com.codepath.blip.models.Blip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Subscriber;

public class BlipListFragment extends DialogFragment {

    public static final String INTENT_BLIPS_LIST = "INTENT_BLIPS_LIST";

    private ArrayList<Blip> mBlips;
    private BlipAdapter mAdapter;
    protected RecyclerView rvBlips;
    protected LinearLayoutManager layoutManager;

    public BlipListFragment() { }

    public static BlipListFragment newInstance(Collection<Blip> blips) {
        BlipListFragment frag = new BlipListFragment();
        Bundle args = new Bundle();
        ArrayList<String> objectIds = new ArrayList<>();
        for (Blip blip : blips) {
            objectIds.add(blip.getUuid());
        }
        args.putStringArrayList(INTENT_BLIPS_LIST, objectIds);
        frag.setArguments(args);
        return frag;
    }

    public static BlipListFragment newInstance(Blip blip) {
        ArrayList<Blip> blipsList = new ArrayList<>();
        blipsList.add(blip);
        return newInstance(blipsList);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_blip_list, container, false);
        rvBlips = (RecyclerView) v.findViewById(R.id.rvBlips);
        rvBlips.setAdapter(mAdapter);
        rvBlips.setLayoutManager(layoutManager);
        Bundle b = getActivity().getIntent().getParcelableExtra("bundle");
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBlips = new ArrayList<>();
        mAdapter = new BlipAdapter(getActivity(), mBlips);
        layoutManager = new LinearLayoutManager(getActivity());
        ArrayList<String> blipIds = getArguments().getStringArrayList(INTENT_BLIPS_LIST);
        assert blipIds != null;
        populateBlips(blipIds);
    }

    public void addAll(List<Blip> b) {
        mBlips.clear();
        mBlips.addAll(b);
        mAdapter.notifyDataSetChanged();
    }

    private void populateBlips(List<String> blipIds) {
        Blip.fromIdList(blipIds).subscribe(new Subscriber<List<Blip>>() {
            @Override
            public void onCompleted() {
                // Nothing
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(List<Blip> blips) {
                addAll(blips);
            }
        });
    }
}
