package com.gmail.maciekhtc.offroadmaps;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by 15936 on 04.06.2016.
 */
public class MapUtils {
    public static HashMap<String,User> userList = new HashMap<>();
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
        return new LatLng(input.getLatitude(),input.getLongitude());
    }
}
