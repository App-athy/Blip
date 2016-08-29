package com.codepath.blip;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.codepath.blip.fragments.BlipListFragment;
import com.codepath.blip.fragments.UserBlipFragment;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if(savedInstanceState == null) {
            UserBlipFragment userTimelineFragment = new UserBlipFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flProfileContainer, userTimelineFragment);
            ft.commit();
        }
    }
}
