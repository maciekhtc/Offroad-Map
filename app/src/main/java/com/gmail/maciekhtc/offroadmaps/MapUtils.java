package com.gmail.maciekhtc.offroadmaps;

import android.app.Notification;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by 15936 on 04.06.2016.
 */
public class MapUtils {
    public static HashMap<String,User> userList = new LinkedHashMap<String,User>();
    public static ArrayList<User> toUpdate = new ArrayList<>();
    public static GoogleMap mMap;

    public static Location locFromLatLng(LatLng input)
    {
        Location result = new Location("");
        result.setLatitude(input.latitude);
        result.setLongitude(input.longitude);
        return result;
    }
    public static LatLng latlngFromLocation(Location input)
    {
        if (input==null) return null;
        return new LatLng(input.getLatitude(),input.getLongitude());
    }
    public static void updateOnlineUsers() {
        try {
            for (int i=toUpdate.size()-1;i>=0;i--) {
                toUpdate.get(i).updateMarker();
            }
            toUpdate.clear();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }
}
