package com.gmail.maciekhtc.offroadmaps;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by 15936 on 05.06.2016.
 */
public class PointUtils {
    public static LinkedList<LinkedList<LatLng>> lines = null;
    public static LinkedList<LatLng> newPoints = new LinkedList();

    public static LinkedList<LatLng> pointsFromFile(LinkedList<String> listString)
    {
        LinkedList<LatLng> filePoints = new LinkedList();
        String []LatLngfromLine = new String[2];
        for (String line: listString)
        {
            LatLngfromLine = line.split(":",2);
            filePoints.add(new LatLng(Double.parseDouble(LatLngfromLine[0]), Double.parseDouble(LatLngfromLine[1])));
        }
        return filePoints;
    }

    public static LinkedList<String> savePoints() {
        LinkedList<String> listString = new LinkedList();
        for (LinkedList<LatLng> line:lines) {
            for (LatLng existingPoint : line) {
                listString.add(existingPoint.latitude+":"+existingPoint.longitude);
            }
        }
        for (LatLng newPoint: newPoints)
        {
            listString.add(newPoint.latitude+":"+newPoint.longitude);
        }
        return listString;
    }

    public static void processNewPoint(Location location) {
        //Log.d("OffroadMap", "Process point");
        LatLng newPoint = MapUtils.latlngFromLocation(location);
        boolean addFlag = true;
        //add only when not too near, only new points to generate new lines, hope it wont be longer than 5sec..
        for (LinkedList<LatLng> line:lines)
        {
            for (LatLng existingPoint:line)
            {
                if (isDistanceSmall(existingPoint,newPoint,5))
                {
                    if (Settings.saveNewPoints) line.set(line.indexOf(existingPoint),new LatLng((existingPoint.latitude + newPoint.latitude) / 2,(existingPoint.longitude + newPoint.longitude) / 2));
                    if (Settings.speakCorners) SpeakUtils.newPosition(line.indexOf(existingPoint),line);
                    //Log.d("OffroadMap", "Found point "+line.indexOf(existingPoint));
                    addFlag=false;
                    break;
                }
            }
            if (!addFlag) break;
        }
        if (addFlag)
        {
            for (LatLng point : newPoints)
            {
                if (isDistanceSmall(point,newPoint,5))
                {
                    if (Settings.saveNewPoints) newPoints.set(newPoints.indexOf(point),new LatLng((point.latitude + newPoint.latitude) / 2,(point.longitude + newPoint.longitude) / 2));
                    if (Settings.speakCorners) SpeakUtils.newPosition(newPoints.indexOf(point), newPoints);
                    addFlag=false;
                    break;
                }
            }
        }
        if (addFlag && Settings.saveNewPoints) newPoints.add(newPoint);

    }

    private static boolean isDistanceSmall(LatLng loc1, LatLng loc2, double highestAcceptable)
    {
        double result=Math.sqrt((loc2.latitude*10000 - loc1.latitude*10000) * (loc2.latitude*10000 - loc1.latitude*10000))+
                             ((loc2.longitude*10000 - loc1.longitude*10000) * (loc2.longitude*10000 - loc1.longitude*10000));
        //Log.d("OffroadMap", "Distance "+result);
        return (result)<highestAcceptable;        //true if smaller than highest acceptable
    }
    public static void getLines(LinkedList<LatLng> filePoints)
    {
        lines = new LinkedList();
        Iterator<LatLng> filePointsIterator = filePoints.iterator();
        while (filePointsIterator.hasNext())
        {
            LinkedList<LatLng> newLine = new LinkedList();
            LatLng loc1 = filePointsIterator.next();
            while (filePointsIterator.hasNext())
            {
                LatLng loc2 = filePointsIterator.next();
                if (isDistanceSmall(loc1,loc2,400)) {
                    newLine.add(loc2);
                    loc1=loc2;
                }
                else break;
            }
            lines.add(newLine);
        }
    }

}
