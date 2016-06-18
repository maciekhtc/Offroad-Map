package com.gmail.maciekhtc.offroadmaps;

import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PositionThread positionThread = null;
    RelativeLayout standardOverlay;
    RelativeLayout settingsOverlay;


    @Override
    public void onBackPressed() {                                                           //zabij proces przy wylaczaniu aplikacji klawiszem back
        positionThread.running = false;
        FileUtils.fileWriteSettings();
        FileUtils.fileWriteLines();
        super.onBackPressed();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        standardOverlay = (RelativeLayout) findViewById(R.id.standartOverlay);
        settingsOverlay = (RelativeLayout) findViewById(R.id.settingsOverlay);

        Button closeSettingsButton = (Button) findViewById(R.id.closeSettingsButton);
        closeSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //action
                Log.d("OffroadMap", "Button Pressed = closeSettingsButton");
            }
        });



        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //

        PointUtils.pointsFromFile();
        positionThread = new PositionThread();
        //positionThread.username="macoo";    //get from textbox?
        positionThread.start();


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapUtils.mMap = this.mMap;

        mMap.setMyLocationEnabled(true);
        //mMap.addPolyline()
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 150, null);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (Settings.followMyPosition)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(MapUtils.latlngFromLocation(location)));
                if (Settings.saveNewPoints) PointUtils.addNewPoint(location);
                if (Settings.updateOnline) {
                    positionThread.myLat = location.getLatitude();
                    positionThread.myLon = location.getLongitude();
                }
                MapUtils.updateOnlineUsers();   //update marker positions from main thread (not positionthread)
            }
        });
    }
}
