package com.gmail.maciekhtc.offroadmaps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by 15936 on 12.06.2016.
 */
public class User {
    public long lastTime;
    public String username;
    public double lat;
    public double lon;
    public Marker marker;
    //

    public User (String lastTime, String username, String lat, String lon) {
        this.lastTime = Long.parseLong(lastTime);
        this.username = username;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        updated();
    }
    public void setParams (String lastTime, String username, String lat, String lon) {
        this.lastTime = Long.parseLong(lastTime);
        this.username = username;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        updated();
    }

    public LatLng getLatLng() {
        return new LatLng(lat,lon);
    }
    private void updated(){
        Log.d("OffroadMap", "Online User: "+username);
        marker.setPosition(getLatLng());
        marker.setTitle(username);
    }
}

