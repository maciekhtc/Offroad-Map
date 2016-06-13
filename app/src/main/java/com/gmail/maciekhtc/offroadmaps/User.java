package com.gmail.maciekhtc.offroadmaps;

import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

/**
 * Created by 15936 on 12.06.2016.
 */
public class User {
    public long lastTime;
    public String username;
    public double lat;
    public double lon;
    public Marker marker = null;
    //

    public User (String lastTime, String username, String lat, String lon) {
        this.lastTime = Long.parseLong(lastTime);
        this.username = username;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        MapUtils.toUpdate.add(this);
    }
    public void setParams (String lastTime, String username, String lat, String lon) {
        this.lastTime = Long.parseLong(lastTime);
        this.username = username;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        MapUtils.toUpdate.add(this);
    }

    public LatLng getLatLng() {
        return new LatLng(lat,lon);
    }
    public void updateMarker(){
        if (marker == null)
        {
            marker = MapUtils.mMap.addMarker(new MarkerOptions()
                    .position(getLatLng())
                    .title(username));
        }
        else
        {
            marker.setPosition(getLatLng());
            marker.setTitle(username);
        }
    }
}

