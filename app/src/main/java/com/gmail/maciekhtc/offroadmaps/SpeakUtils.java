package com.gmail.maciekhtc.offroadmaps;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by 15936 on 21.06.2016.
 */
public class SpeakUtils {

    private static int indexOfPointOld = -1;
    private static ArrayList<LatLng> currentLineOld = null;
    private static boolean watchOut = false;
    public static TextToSpeech tts;
    private static String cornerMessage="";

    public static void newPosition(int indexOfPoint, ArrayList<LatLng> currentLine)
    {
        if (indexOfPoint!=-1 && indexOfPoint<currentLine.size()) {
            for (LatLng junction : PointUtils.junctionPoints)
            {
                if (PointUtils.calculateDistance(currentLine.get(indexOfPoint),junction)<10)
                {
                    roadCross();
                    break;
                }
            }
        }
        //
        if (currentLine == currentLineOld)
        {
            if (indexOfPoint > indexOfPointOld)
            {   //moving to higher index
                if (currentLine.size()>=indexOfPoint+8)
                {
                    LatLng point1=currentLine.get(indexOfPoint+2);
                    LatLng point2=currentLine.get(indexOfPoint+5);
                    LatLng point3=currentLine.get(indexOfPoint+8);
                    Log.d("OffroadMap", "moving to higher index");
                    corner(calculateAngle(point1, point2, point3));
                    watchOut = false;
                }
                else
                {
                    roadCross();
                }
            }
            else if (indexOfPoint < indexOfPointOld)
            {   //moving to lower index
                if (0<=indexOfPoint-8)
                {
                    LatLng point1=currentLine.get(indexOfPoint-2);
                    LatLng point2=currentLine.get(indexOfPoint-5);
                    LatLng point3=currentLine.get(indexOfPoint-8);
                    Log.d("OffroadMap", "moving to lower index");
                    corner(calculateAngle(point1, point2, point3));
                    watchOut = false;
                }
                else
                {
                    roadCross();
                }
            }
            else
            {   //standing still
                //MapUtils.mMap.addMarker(new MarkerOptions().position(currentLine.get(indexOfPoint)));
            }
        }
        else
        {   //change line, road cross?
            //Log.d("OffroadMap", "Line change");
            roadCross();
        }
        //update positiond
        currentLineOld = currentLine;
        indexOfPointOld = indexOfPoint;
    }

    private static double calculateAngle(LatLng point1, LatLng point2, LatLng point3) {
        double alpha = Math.atan2(point1.latitude - point2.latitude, point1.longitude - point2.longitude);
        double beta = Math.atan2(point3.latitude-point2.latitude,point3.longitude-point2.longitude);
        double result = Math.toDegrees(alpha)-Math.toDegrees(beta);
        if (result > 180) result = -(360 - result);
        else if (result < -180) result = (result + 360);
        Log.d("OffroadMap", "Angle: " + result + " / " + point1.toString() + " " + point2.toString() + " " + point3.toString());
        return (result);
    }

    private static void roadCross() {
        if (!watchOut)
        {
            tts.speak("Uważaj!", TextToSpeech.QUEUE_ADD, null);
            watchOut = true;
        }
    }
    private static void corner(double angle) {
        double cornerAngle = angle;
        //Log.d("OffroadMap", "Angle:" + cornerAngle);
        String message;
        if (cornerAngle > 0)
        {
            message="lewy ";
        }
        else
        {
            cornerAngle=-cornerAngle;
            message = "prawy ";
        }
        if (cornerAngle < 5) message = "";  ///////
        else if (cornerAngle < 70) message+="1";
        else if (cornerAngle < 110) message+="2";
        else if (cornerAngle < 135) message+="3";
        else if (cornerAngle < 150) message+="4";
        else if (cornerAngle < 160) message+="5";
        else if (cornerAngle < 170) message+="6";
        else if (cornerAngle <= 180) message="";    //straight
        else message="błąd";   //not needed ?
        if (!cornerMessage.contentEquals("")&&!message.contentEquals("")) message = "do " + message;
        if (!message.contentEquals("")) tts.speak(message, TextToSpeech.QUEUE_ADD, null);
        cornerMessage = message;
    }
}
