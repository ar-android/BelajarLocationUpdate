package com.ahmadrosid.belajarlocationupdate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ahmadrosid.belajarlocationupdate.utils.LocationUtils;

public class MainActivity extends AppCompatActivity {

    LocationUtils locationUtils;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);

        locationUtils = new LocationUtils(this);
        locationUtils.checkGpsService();
        locationUtils.onStartLocationUtils();

        locationUtils.setLocationCallback(
                lastLocation -> {
                    text.append("LastLocation Latitude : " + lastLocation.getLatitude());
                    text.append("\nLastLocation Longitude : " + lastLocation.getLongitude());
                },
                locationUpdate -> {
                    text.append("\nLastUpdate Latitude : " + locationUpdate.getLatitude());
                    text.append("\nLastUpdate Longitude : " + locationUpdate.getLongitude());
                }
        );
    }

    @Override protected void onStop() {
        super.onStop();
        locationUtils.onDestroyLocationUtils();
    }
}
