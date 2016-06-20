package com.gmail.maciekhtc.offroadmaps;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by 15936 on 04.06.2016.
 */
public class MapUtils {
    public static HashMap<String,User> userList = new LinkedHashMap<String,User>();
    public static ArrayList<User> toUpdate = new ArrayList<>();
    public static ArrayList<User> usersWithMessage = new ArrayList<>();
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
    public static void updateOnlineUsers() {
        for (User u:toUpdate)
        {
            u.updateMarker();
        }
        toUpdate.clear();
    }
    public static void processMessages()
    {
        ArrayList<Integer> toRemove = new ArrayList();
        for (User u:usersWithMessage)
        {
            if (u.messageTimeout==5)
            {
                                                //todo speak message now
                u.marker.setSnippet(u.message);
                u.marker.showInfoWindow();
                u.messageTimeout--;
            }
            else if (u.messageTimeout == 0)
            {
                u.marker.hideInfoWindow();
                u.marker.setSnippet(null);
                toRemove.add(usersWithMessage.indexOf(u));
            }
            else
            {
                u.messageTimeout--;
            }
        }
        for (Integer id:toRemove)
        {
            //usersWithMessage.remove(id);
        }
    }
}
