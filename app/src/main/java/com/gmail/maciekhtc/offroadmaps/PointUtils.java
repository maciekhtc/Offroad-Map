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
    public static ArrayList<ArrayList<LatLng>> lines = null;
    public static ArrayList<LatLng> newPoints = new ArrayList();
    public static ArrayList<LatLng> junctionPoints = new ArrayList();

    public static ArrayList<LatLng> pointsFromFile(ArrayList<String> listString)
    {
        ArrayList<LatLng> filePoints = new ArrayList();
        String []LatLngfromLine = new String[2];
        for (String line: listString)
        {
            LatLngfromLine = line.split(":",2);
            filePoints.add(new LatLng(Double.parseDouble(LatLngfromLine[0]), Double.parseDouble(LatLngfromLine[1])));
        }
        return filePoints;
    }

    public static ArrayList<String> savePoints() {
        ArrayList<String> listString = new ArrayList();
        for (ArrayList<LatLng> line:lines) {
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
        for (int index = newPoints.size()-2; index>=0; index--) //check all without last added point, in reverse to find last matching point faster when standing still
        {                                           //without last added because it will help making points closer to each other still with no error
            LatLng point = newPoints.get(index);
            if (calculateDistance(point,newPoint)<6)
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
                    //todo if distance becomes really high it means new line was started here so previous point is junction point?
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
            for (int lineId = lines.size()-1; lineId>=0 ;lineId--)
            {
                ArrayList<LatLng> line = lines.get(lineId);
                for (LatLng existingPoint:line)     //maybe iterate by index to fix problem with modification of point
                {
                    if (calculateDistance(existingPoint,newPoint)<8)
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
        if (loc1 == null || loc2 == null) return 1000;
        double result=Math.sqrt((loc2.latitude*10000 - loc1.latitude*10000) * (loc2.latitude*10000 - loc1.latitude*10000))+
                             ((loc2.longitude*10000 - loc1.longitude*10000) * (loc2.longitude*10000 - loc1.longitude*10000));
        //Log.d("OffroadMap", "Distance "+result);
        return result;
    }
    public static void getLines(ArrayList<LatLng> filePoints)
    {
        int count=0;
        lines = new ArrayList();
        Iterator<LatLng> filePointsIterator = filePoints.iterator();
        LatLng loc1 = null;
        if (filePointsIterator.hasNext()) loc1 = filePointsIterator.next();
        while (filePointsIterator.hasNext())
        {
            ArrayList<LatLng> newLine = new ArrayList();
            newLine.add(loc1);
            count++;
            boolean addNewLine = true;
            while (filePointsIterator.hasNext())
            {
                LatLng loc2 = filePointsIterator.next();
                if (calculateDistance(loc1,loc2)<15) {  //when points are too far break and create new line
                    loc1=loc2;
                    newLine.add(loc1);
                    count++;
                }
                else {
                    for (ArrayList<LatLng> existingLine : lines)
                    {
                        if (calculateDistance(loc1,existingLine.get(0))<15) {   //paste this at the beginning
                            existingLine.addAll(0,newLine);
                            addNewLine = false;
                            break;
                        }
                        else if (calculateDistance(loc1,existingLine.get(existingLine.size()-1))<15)    //paste this at the end
                        {
                            existingLine.addAll(newLine);
                            addNewLine = false;
                            break;
                        }
                    }
                    loc1=loc2;
                    break;
                }
            }
            if (addNewLine) lines.add(newLine);
        }
        optimizeLines();
        Log.d("OffroadMap","Lined points: "+count+", in file: "+filePoints.size());
    }
    private static LatLng modifyPoint(LatLng existingPoint, LatLng newPoint)
    {
        double factor=0.05;
        double newLatitude = ((existingPoint.latitude * (1 - factor)) + (newPoint.latitude * factor));
        double newLongitude = ((existingPoint.longitude * (1 - factor)) + (newPoint.longitude * factor));
        return new LatLng(newLatitude,newLongitude);
    }
    private static void optimizeLines()
    {
        //add junction points
        //add to the beginning or ending of line the point which is the clone of nearest point from another line (with checking for short distance)
        //and then add to the list of junction points to prevent moving those points and to be able to warn user about crossing lines with another route
        for (ArrayList<LatLng> line : lines)
        {
            for (ArrayList<LatLng> comparedLine : lines) {
                if (line != comparedLine)
                {
                    for (LatLng comparedPoint : comparedLine) {
                        if (calculateDistance(comparedPoint, line.get(0)) < 15) {
                            int startIndex = line.indexOf(comparedPoint);
                            LatLng bestPoint = comparedPoint;
                            for (int i=1;i<12;i++)
                            {
                                if (startIndex+i>=comparedLine.size()) break;
                                if (calculateDistance(bestPoint, line.get(0)) > calculateDistance(comparedLine.get(startIndex+i),line.get(0)))
                                {
                                    bestPoint=comparedLine.get(startIndex+i);
                                }
                            }
                            if (calculateDistance(bestPoint, line.get(0)) == 0) line.add(0,bestPoint);
                            junctionPoints.add(bestPoint);
                            break;
                        } else if (calculateDistance(comparedPoint, line.get(line.size() - 1)) < 15) {
                            int startIndex = line.indexOf(comparedPoint);
                            LatLng bestPoint = comparedPoint;
                            for (int i=1;i<12;i++)
                            {
                                if (startIndex+i>=comparedLine.size()) break;
                                if (calculateDistance(bestPoint, line.get(line.size() - 1)) > calculateDistance(comparedLine.get(startIndex+i),line.get(line.size() - 1)))
                                {
                                    bestPoint=comparedLine.get(startIndex+i);
                                }
                            }
                            if (calculateDistance(bestPoint, line.get(line.size() - 1)) == 0) line.add(bestPoint);
                            junctionPoints.add(bestPoint);
                            break;
                        }
                    }
                }
            }
        }
    }

}
