package com.ahmadrosid.belajarlocationupdate.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import rx.subscriptions.CompositeSubscription;

import static android.content.Context.LOCATION_SERVICE;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;

/**
 * Created by ocittwo on 2/26/17.
 *
 * @Author Ahmad Rosid
 * @Email ocittwo@gmail.com
 * @Github https://github.com/ar-android
 * @Web http://ahmadrosid.com
 */
public class LocationUtils implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationImpl {

    static final String TAG = "LocationUtils";

    String[] REQUEST_TYPE_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    CompositeSubscription subscription = new CompositeSubscription();

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    Context context;

    LastLocation lastLocation;
    LocationUpdate locationUpdate;

    public LocationUtils(Context context) {
        this.context = context;
        mLocationRequest = new LocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Log.d(TAG, "LocationUtils: isCreated");
    }


    public void setLocationCallback(LastLocation lastLocation, LocationUpdate locationUpdate) {
        this.lastLocation = lastLocation;
        this.locationUpdate = locationUpdate;
    }

    public void checkGpsService() {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage("Please activate gps setting to continue.")
                .setCancelable(false)
                .setPositiveButton("Open Setting",
                        (dialog, id) -> {
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(callGPSSettingIntent);
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    protected void createLocationRequest() {
        Log.d(TAG, "createLocationRequest: true");
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: true");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            subscription.add(
                    RxPermissions.get(context)
                            .observe(REQUEST_TYPE_PERMISSION)
                            .subscribe(granted -> {
                                if (granted) {
                                    requestLocation();
                                } else {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                    alertDialogBuilder.setMessage("Permission no granted please give permission to continue.")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok",
                                                    (dialog, id) -> startLocationUpdates());
                                    AlertDialog alert = alertDialogBuilder.create();
                                    alert.show();
                                }
                            })
            );
            return;
        } else {
            requestLocation();
        }
    }

    private void requestLocation() {
        // Location update with interval 60000 ms
        FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        // Location las location update
        mLastLocation = FusedLocationApi.getLastLocation(mGoogleApiClient);

        Log.d(TAG, "lastLatitude: " + mLastLocation.getLatitude());
        Log.d(TAG, "lastLongitude: " + mLastLocation.getLongitude());

        lastLocation.onLastLocationCallback(mLastLocation);
    }

    @Override public void onLocationChanged(Location location) {
        Log.d(TAG, "Latitude : " + location.getLatitude());
        Log.d(TAG, "Longitude : " + location.getLongitude());
        locationUpdate.onLocationUpdate(location);
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GoogleApiClient is Connected");
        createLocationRequest();
        startLocationUpdates();
    }

    @Override public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    @Override public void onDestroyLocationUtils() {
        mGoogleApiClient.disconnect();
        subscription.unsubscribe();
        FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient = null;
    }

    @Override public void onStartLocationUtils() {
        mGoogleApiClient.connect();
    }

    public interface LastLocation {
        void onLastLocationCallback(Location location);
    }

    public interface LocationUpdate {
        void onLocationUpdate(Location location);
    }
}
