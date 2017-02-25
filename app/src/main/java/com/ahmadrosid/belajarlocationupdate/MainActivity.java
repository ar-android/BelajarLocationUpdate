package com.ahmadrosid.belajarlocationupdate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ahmadrosid.belajarlocationupdate.utils.LocationUtils;

public class MainActivity extends AppCompatActivity {

    LocationUtils locationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationUtils = new LocationUtils(this);
        locationUtils.checkGpsService();
        locationUtils.onStartLocationUtils();
    }

    @Override protected void onStop() {
        super.onStop();
        locationUtils.onDestroyLocationUtils();
    }
}
