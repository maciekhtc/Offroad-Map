package com.gmail.maciekhtc.offroadmaps;

import android.location.Location;
import android.speech.tts.TextToSpeech;
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
    public static boolean linesReady = false;
    public static ArrayList<LatLng> newPoints = new ArrayList();
    public static ArrayList<LatLng> junctionPoints = new ArrayList();
    public static final double limitValue = 6.0;//6
    private static boolean lineEnded = true;
    private static LatLng modPoint = null;

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
        //Log.d("OffroadMap", "ProcessPoint start");
        LatLng newPoint = MapUtils.latlngFromLocation(location);
        boolean addFlag = true;
        //add only when not too near, only new points to generate new lines, hope it wont be longer than 5sec..
        int newPointsSize = newPoints.size();
        for (int index = newPointsSize-1; index>=0; index--) //check all, in reverse to find last matching point faster when standing still
        {
            LatLng point = newPoints.get(index);
            if (calculateDistance(point,newPoint)<limitValue*0.3)
            {
                //to powinno byc przed petla? ale przy wyszukiwaniu posrod wszystkich punktow nawet tych ostatnich to nie ma znaczenia:
                //prevent recording points when started going back
                /*
                if ((calculateDistance(newPoint,newPoints.get(newPointsSize-2)) <
                        calculateDistance(newPoints.get(newPointsSize-1),newPoints.get(newPointsSize-2))) &&
                        calculateDistance(newPoints.get(newPointsSize-1),newPoints.get(newPointsSize-2)) < limitValue*0.3)
                {
                    addFlag = false;
                    break;
                }
                */
                //
                int startIndex = newPoints.indexOf(point);
                LatLng bestPoint = point;
                int indexDelta=0;
                for (int i=1;i<30;i++)
                {
                    if (startIndex+i >= newPointsSize) break;
                    if (calculateDistance(bestPoint,newPoint) > calculateDistance(newPoints.get(startIndex+i),newPoint))
                    {
                        bestPoint=newPoints.get(startIndex+i);
                        indexDelta=i;
                    }
                }
                //do not speak for 5 latest added points
                if (Settings.speakCorners && ((startIndex+indexDelta)<(newPointsSize-5))) SpeakUtils.newPosition((startIndex+indexDelta), newPoints);
                //modPoint = modifyPoint(bestPoint, newPoint);
                modPoint = bestPoint;
                //if (Settings.saveNewPoints) newPoints.set((startIndex+indexDelta), modPoint);
                addFlag=false;
                Log.d("OffroadMap", "ProcessPoint new modified");
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
                    if (calculateDistance(existingPoint,newPoint)<limitValue*0.5)     //from 0.3 to 1
                    {
                        int startIndex = line.indexOf(existingPoint);
                        LatLng bestPoint = existingPoint;
                        int indexDelta=0;
                        for (int i=1;i<30;i++)
                        {
                            if (startIndex+i >= line.size()) break;
                            if (calculateDistance(bestPoint, newPoint) > calculateDistance(line.get(startIndex+i),newPoint))
                            {
                                bestPoint=line.get(startIndex+i);
                                indexDelta=i;
                            }
                        }
                        boolean flag = true;
                        for (LatLng junction : junctionPoints)
                        {
                            if (calculateDistance(bestPoint,junction)==0)
                            {
                                flag = false;
                                break;
                            }
                        }
                        if (Settings.speakCorners) SpeakUtils.newPosition((startIndex+indexDelta), line);
                        if (Settings.saveNewPoints && flag) {
                            //modPoint = modifyPoint(bestPoint, newPoint);
                            modPoint = bestPoint;
                            //line.set((startIndex+indexDelta), modPoint);
                            Log.d("OffroadMap", "ProcessPoint old modified");
                        }
                        addFlag=false;
                        break;
                    }
                }
                if (!addFlag) break;
            }
        }
        if (addFlag && (addFlag == lineEnded) && Settings.saveNewPoints && modPoint!=null) {
            newPoints.add(modPoint);
            junctionPoints.add(modPoint);
        }
        if (addFlag && Settings.saveNewPoints){
            newPoints.add(newPoint);
            Log.d("OffroadMap","ProcessPoint added");
        }
        lineEnded = !addFlag;
    }

    public static double calculateDistance(LatLng loc1, LatLng loc2)
    {
        if (loc1 == null || loc2 == null) return 1000;
        //return result=Math.sqrt((loc2.latitude*20000 - loc1.latitude*20000) * (loc2.latitude*20000 - loc1.latitude*20000))+((loc2.longitude*10000 - loc1.longitude*10000) * (loc2.longitude*10000 - loc1.longitude*10000));
        //float result[] = new float[1]; Location.distanceBetween(loc1.latitude,loc1.longitude,loc2.latitude,loc2.longitude,result); return result[0];
        double x = (loc2.longitude - loc1.longitude) * Math.cos((loc1.latitude + loc2.latitude) / 2);
        double y = (loc2.latitude - loc1.latitude);
        return Math.sqrt(x * x + y * y) * 6371;
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
                if (calculateDistance(loc1,loc2)<limitValue) {  //when points are too far break and create new line
                    loc1=loc2;
                    newLine.add(loc1);
                    count++;
                }
                else {
                    //loc1 is last point from newLine, loc2 is the next point which will be first point in next line
                    loc1=loc2;
                    break;
                }
            }
            //check at the end, when there is no more points and last point was good
            ArrayList<LatLng> lineToConcatenateSecondTime = null;
            for (ArrayList<LatLng> existingLine : lines)
            {
                if (calculateDistance(newLine.get(newLine.size()-1),existingLine.get(0))<limitValue) {   //paste this at the beginning               //OK
                    existingLine.addAll(0,newLine);
                    addNewLine = false;
                    lineToConcatenateSecondTime = existingLine;
                    break;
                }
                else if (calculateDistance(newLine.get(newLine.size()-1),existingLine.get(existingLine.size()-1))<limitValue)    //paste this at the end     //REVERSED!
                {
                    //existingLine.addAll(newLine);
                    for (int indexForReverse=newLine.size()-1;indexForReverse>=0;indexForReverse--)
                    {
                        existingLine.add(newLine.get(indexForReverse));
                    }
                    addNewLine = false;
                    lineToConcatenateSecondTime = existingLine;
                    break;
                }

                else if (calculateDistance(newLine.get(0),existingLine.get(0))<limitValue)    //at the beginning            //REVERSED one by one
                {
                    //existingLine.addAll(0,newLine);
                    for (int indexForReverse=0;indexForReverse<newLine.size();indexForReverse++)
                    {
                        existingLine.add(0,newLine.get(indexForReverse));
                    }
                    addNewLine = false;
                    lineToConcatenateSecondTime = existingLine;
                    break;
                }
                else if (calculateDistance(newLine.get(0),existingLine.get(existingLine.size()-1))<limitValue)  //at the end    //OK
                {
                    existingLine.addAll(newLine);
                    addNewLine = false;
                    lineToConcatenateSecondTime = existingLine;
                    break;
                }
            }
            boolean keepConcatenatedLine = true;
            if (lineToConcatenateSecondTime!=null) {
                for (ArrayList<LatLng> existingLine : lines) {
                    if (existingLine==lineToConcatenateSecondTime) continue;
                    if (calculateDistance(lineToConcatenateSecondTime.get(lineToConcatenateSecondTime.size() - 1), existingLine.get(0)) < limitValue) {
                        existingLine.addAll(0, lineToConcatenateSecondTime);
                        keepConcatenatedLine = false;
                        break;
                    } else if (calculateDistance(lineToConcatenateSecondTime.get(lineToConcatenateSecondTime.size() - 1), existingLine.get(existingLine.size() - 1)) < limitValue)    //paste this at the end     //REVERSED!
                    {
                        //existingLine.addAll(newLine);
                        for (int indexForReverse = lineToConcatenateSecondTime.size() - 1; indexForReverse >= 0; indexForReverse--) {
                            existingLine.add(lineToConcatenateSecondTime.get(indexForReverse));
                        }
                        keepConcatenatedLine = false;
                        break;
                    } else if (calculateDistance(lineToConcatenateSecondTime.get(0), existingLine.get(0)) < limitValue)    //at the beginning            //REVERSED one by one
                    {
                        //existingLine.addAll(0,newLine);
                        for (int indexForReverse = 0; indexForReverse < lineToConcatenateSecondTime.size(); indexForReverse++) {
                            existingLine.add(0, lineToConcatenateSecondTime.get(indexForReverse));
                        }
                        keepConcatenatedLine = false;
                        break;
                    } else if (calculateDistance(lineToConcatenateSecondTime.get(0), existingLine.get(existingLine.size() - 1)) < limitValue)  //at the end    //OK
                    {
                        existingLine.addAll(lineToConcatenateSecondTime);
                        keepConcatenatedLine = false;
                        break;
                    }
                }
            }
            if (!keepConcatenatedLine) lines.remove(lineToConcatenateSecondTime);
            if (addNewLine) lines.add(newLine);
        }
        optimizeLines();
        linesReady = true;
        Log.d("OffroadMap", "Lined points: " + count + ", in file: " + filePoints.size()+", in lines: "+lines.size());
    }
    private static LatLng modifyPoint(LatLng existingPoint, LatLng newPoint)
    {
        double factor=0.02;
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
                for (int indexOfComparedPoint = 0;indexOfComparedPoint<comparedLine.size();indexOfComparedPoint++) {
                    LatLng comparedPoint = comparedLine.get(indexOfComparedPoint);
                    if (!(comparedLine==line && (indexOfComparedPoint > comparedLine.size()-8 || indexOfComparedPoint < 8))) {
                        if (calculateDistance(comparedPoint, line.get(0)) < limitValue) {
                            int startIndex = comparedLine.indexOf(comparedPoint);
                            LatLng bestPoint = comparedPoint;
                            for (int i = 1; i < 30; i++) {
                                if (startIndex + i >= comparedLine.size()) break;
                                if (calculateDistance(bestPoint, line.get(0)) > calculateDistance(comparedLine.get(startIndex + i), line.get(0))) {
                                    bestPoint = comparedLine.get(startIndex + i);
                                }
                            }
                            if (calculateDistance(bestPoint, line.get(0)) != 0) {
                                line.add(0, bestPoint);
                                Log.d("OffroadMap", "Add Junction " + bestPoint.toString());
                            }
                            junctionPoints.add(bestPoint);
                            Log.d("OffroadMap", "Found junction " + bestPoint.toString());
                            break;
                        } else if (calculateDistance(comparedPoint, line.get(line.size() - 1)) < limitValue) {
                            int startIndex = comparedLine.indexOf(comparedPoint);
                            LatLng bestPoint = comparedPoint;
                            for (int i = 0; i < 30; i++) {
                                if (startIndex + i >= comparedLine.size()) break;
                                if (calculateDistance(bestPoint, line.get(line.size() - 1)) > calculateDistance(comparedLine.get(startIndex + i), line.get(line.size() - 1))) {
                                    bestPoint = comparedLine.get(startIndex + i);
                                }
                            }
                            if (calculateDistance(bestPoint, line.get(line.size() - 1)) != 0) {
                                line.add(bestPoint);
                                Log.d("OffroadMap", "Add Junction " + bestPoint.toString());
                            }
                            junctionPoints.add(bestPoint);
                            Log.d("OffroadMap", "Found Junction " + bestPoint.toString());
                            break;
                        }
                    }
                    else break;
                }
            }
        }
    }

}
