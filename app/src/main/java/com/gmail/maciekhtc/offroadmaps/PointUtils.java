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
        int newPointsSize = newPoints.size();
        for (LatLng point : newPoints)
        {
            if (calculateDistance(point,newPoint)<7)
            {
                int startIndex = newPoints.indexOf(point);
                LatLng bestPoint = point;
                for (int i=1;i<12;i++)
                {
                    if (startIndex+i>=newPoints.size()) break;
                    if (calculateDistance(bestPoint,newPoint) > calculateDistance(newPoints.get(startIndex+i),newPoint))
                    {
                        bestPoint=newPoints.get(startIndex+i);
                    }
                }
                if (Settings.saveNewPoints) newPoints.set(newPoints.indexOf(bestPoint),modifyPoint(bestPoint, newPoint));
                //do not speak for 5 latest added points
                if (Settings.speakCorners && (newPoints.indexOf(point)<(newPointsSize-5))) SpeakUtils.newPosition(newPoints.indexOf(bestPoint), newPoints);
                addFlag=false;
                break;
            }
        }
        if (addFlag)
        {
            while (lines.descendingIterator().hasNext())
            {
                LinkedList<LatLng> line = lines.descendingIterator().next();
                for (LatLng existingPoint:line)     //maybe iterate by index to fix problem with modification of point
                {
                    if (calculateDistance(existingPoint,newPoint)<10)
                    {
                        int startIndex = line.indexOf(existingPoint);
                        LatLng bestPoint = existingPoint;
                        for (int i=1;i<12;i++)
                        {
                            if (startIndex+i>=line.size()) break;
                            if (calculateDistance(bestPoint, newPoint) > calculateDistance(line.get(startIndex+i),newPoint))
                            {
                                bestPoint=line.get(startIndex+i);
                            }
                        }
                        if (Settings.saveNewPoints) line.set(line.indexOf(bestPoint),modifyPoint(bestPoint, newPoint));
                        if (Settings.speakCorners) SpeakUtils.newPosition(line.indexOf(bestPoint), line);
                        addFlag=false;
                        break;
                    }
                }
                if (!addFlag) break;
            }
        }
        if (addFlag && Settings.saveNewPoints) newPoints.add(newPoint);

    }

    private static double calculateDistance(LatLng loc1, LatLng loc2)
    {
        double result=Math.sqrt((loc2.latitude*10000 - loc1.latitude*10000) * (loc2.latitude*10000 - loc1.latitude*10000))+
                             ((loc2.longitude*10000 - loc1.longitude*10000) * (loc2.longitude*10000 - loc1.longitude*10000));
        //Log.d("OffroadMap", "Distance "+result);
        return result;
    }
    public static void getLines(LinkedList<LatLng> filePoints)
    {
        int count=0;
        lines = new LinkedList();
        Iterator<LatLng> filePointsIterator = filePoints.iterator();
        LatLng loc1 = filePointsIterator.next();
        while (filePointsIterator.hasNext())
        {
            LinkedList<LatLng> newLine = new LinkedList();
            newLine.add(loc1);
            count++;
            while (filePointsIterator.hasNext())
            {
                LatLng loc2 = filePointsIterator.next();
                if (calculateDistance(loc1,loc2)<50) {  //when points are too far break and create new line
                    loc1=loc2;
                    newLine.add(loc1);
                    count++;
                }//todo search for any other near point, to concatenate lines
                else {
                    loc1=loc2;
                    break;
                }
            }
            lines.add(newLine);
        }
        //todo optimize lines by adding nearest point to the end and beginning (if distance lower than ##)
        Log.d("OffroadMap","Lined points: "+count+", in file: "+filePoints.size());
    }
    private static LatLng modifyPoint(LatLng existingPoint, LatLng newPoint)
    {
        double factor=0.15;
        double newLatitude = ((existingPoint.latitude * (1 - factor)) + (newPoint.latitude * factor));
        double newLongitude = ((existingPoint.longitude * (1 - factor)) + (newPoint.longitude * factor));
        return new LatLng(newLatitude,newLongitude);
    }

}
