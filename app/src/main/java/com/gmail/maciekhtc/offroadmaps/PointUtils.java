package com.gmail.maciekhtc.offroadmaps;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by 15936 on 05.06.2016.
 */
public class PointUtils {
    public static ArrayList<LatLng> filePoints = new ArrayList();
    public static ArrayList<LatLng> newPoints = new ArrayList();

    public static void pointsFromFile()
    {
        ArrayList<String> listString = FileUtils.fileInit();
        String []LatLngfromLine = new String[2];
        for (String line: listString)
        {
            LatLngfromLine = line.split(":",2);
            filePoints.add(new LatLng(Double.parseDouble(LatLngfromLine[0]), Double.parseDouble(LatLngfromLine[1])));
        }
    }

    public static void savePoints() {
        ArrayList<String> listString = new ArrayList();
        for (LatLng point: newPoints)
        {
            listString.add(point.latitude+":"+point.longitude);
        }
        FileUtils.fileWriteLines(listString);
    }

    public static void addNewPoint(Location location) {
        newPoints.add(MapUtils.latlngFromLocation(location));
    }
}