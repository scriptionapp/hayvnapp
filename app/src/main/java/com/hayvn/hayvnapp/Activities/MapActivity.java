package com.hayvn.hayvnapp.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Utilities.PermissionUtils;
import com.hayvn.hayvnapp.databinding.ActivityMainappBinding;
import com.hayvn.hayvnapp.databinding.ActivityMapBinding;

import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.IntentConstants.LAT_LNG;

public class MapActivity extends BaseParentActivity implements
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private static final String TAG = "MAP_ACT";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    GoogleMap myMap;
    LatLng mLocation;

    Marker marker;

    private ActivityMapBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        binding.coordsSelect.setVisibility(View.GONE);

        binding.coordsSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(LAT_LNG, mLocation);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Intent intent = getIntent();
        try {
            mLocation = (LatLng) Objects.requireNonNull(intent.getExtras()).get(LAT_LNG);
            Log.d(TAG, "Got location from intent");
        } catch (NullPointerException e){
            //
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    mLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    addNewMarker(mLocation);
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady");
        myMap = map;
        UiSettings uiSettings  = map.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);

        if (mLocation != null){
            addNewMarker(mLocation);
            marker.showInfoWindow();
            binding.coordsSelect.setVisibility(View.VISIBLE);
        }

        map.setOnMapClickListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        enableMyLocation();
        getLocation();
    }

    private void enableMyLocation() {
        Log.d(TAG, "enableMyLocation");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (myMap != null) {
                myMap.setMyLocationEnabled(true);
            }
        } else {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        Log.d(TAG, "onResumeFragments");
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    protected void createLocationRequest(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "Success");
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Log.d(TAG, "Fail");
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapActivity.this,
                                30);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void getLocation(){
        Log.d(TAG, "getLocation");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (mLocation == null && location != null) {
                            mLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            addNewMarker(mLocation);
                            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(),
                                            location.getLongitude()), myMap.getCameraPosition().zoom+0.5f));
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Fail");
                createLocationRequest();
            }
        });
    }

    @Override
    public void onMapClick(LatLng point) {
        mLocation = point;
        addNewMarker(point);
        marker.showInfoWindow();
    }


    @Override
    public void onMarkerDragStart(Marker marker) {
        //
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        mLocation = marker.getPosition();
        marker.setTitle(mLocation.toString());
        marker.showInfoWindow();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mLocation = marker.getPosition();
        marker.setTitle(mLocation.toString());
        marker.showInfoWindow();
    }

    private void addNewMarker(LatLng location) {
        if(location != null){
            if (marker != null) marker.remove();
            binding.coordsSelect.setVisibility(View.VISIBLE);
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, myMap.getCameraPosition().zoom+0.5f));
            marker = myMap.addMarker(new MarkerOptions()
                    .position(location)
                    .draggable(true)
                    .title(location.toString()));;

        }

    }

    @Override
    public boolean onMyLocationButtonClick() {
        if(mLocation != null){
            addNewMarker(mLocation);
        }
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        if(location != null){
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            addNewMarker(ll);
        }
    }
}
