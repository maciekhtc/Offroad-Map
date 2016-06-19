package com.gmail.maciekhtc.offroadmaps;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by 15936 on 05.06.2016.
 */
public class PointUtils {
    public static LinkedList<LatLng> filePoints = new LinkedList();
    public static LinkedList<LatLng> newPoints = new LinkedList();

    public static void pointsFromFile(LinkedList<String> listString)
    {
        String []LatLngfromLine = new String[2];
        for (String line: listString)
        {
            LatLngfromLine = line.split(":",2);
            filePoints.add(new LatLng(Double.parseDouble(LatLngfromLine[0]), Double.parseDouble(LatLngfromLine[1])));
        }
    }

    public static LinkedList<String> savePoints() {
        LinkedList<String> listString = new LinkedList();
        for (LatLng point: newPoints)
        {
            listString.add(point.latitude+":"+point.longitude);
        }
        return listString;
    }

    public static void addNewPoint(Location location) {
        if (location.getAccuracy()<20) newPoints.add(MapUtils.latlngFromLocation(location));
    }

    private static boolean isDistanceSmall(LatLng loc1, LatLng loc2)
    {
        double result=Math.sqrt(Math.pow(loc2.latitude-loc1.latitude,2)+Math.pow(loc2.longitude-loc1.longitude,2));
        double highestAcceptable = 0.15;
        return result<highestAcceptable;        //true if smaller than highest acceptable
    }
}
