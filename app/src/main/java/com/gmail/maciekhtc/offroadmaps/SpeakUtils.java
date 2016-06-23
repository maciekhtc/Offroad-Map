package com.gmail.maciekhtc.offroadmaps;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedList;

/**
 * Created by 15936 on 21.06.2016.
 */
public class SpeakUtils {

    private static int indexOfPointOld = -1;
    private static LinkedList<LatLng> currentLineOld = null;
    private static boolean watchOut = false;
    public static TextToSpeech tts;

    //SpeakUtils.tts.speak(username+":"+message, TextToSpeech.QUEUE_ADD, null);

    public static void newPosition(int indexOfPoint, LinkedList<LatLng> currentLine)
    {
        if (currentLine == currentLineOld)
        {
            if (indexOfPoint > indexOfPointOld)
            {   //moving to higher index
                if (currentLine.size()>=indexOfPoint+5)
                {
                    LatLng point1=currentLine.get(indexOfPoint+1);
                    LatLng point2=currentLine.get(indexOfPoint+3);
                    LatLng point3=currentLine.get(indexOfPoint+5);
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
                if (0<=indexOfPoint-4)
                {
                    LatLng point1=currentLine.get(indexOfPoint);
                    LatLng point2=currentLine.get(indexOfPoint-2);
                    LatLng point3=currentLine.get(indexOfPoint-4);
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
                Log.d("OffroadMap", "Standing still: " + indexOfPoint + "/" + currentLine.size());
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
        return (Math.toDegrees(alpha)-Math.toDegrees(beta));
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
        Log.d("OffroadMap", "Angle:" + cornerAngle);
        if (cornerAngle > 180) cornerAngle = 180 - cornerAngle;         //??
        else if (cornerAngle < -180) cornerAngle = -(cornerAngle + 180);//??
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
        else if (cornerAngle < 50) message+="1";
        else if (cornerAngle < 100) message+="2";
        else if (cornerAngle < 130) message+="3";
        else if (cornerAngle < 140) message+="4";
        else if (cornerAngle < 150) message+="5";
        else if (cornerAngle < 160) message+="6";
        else if (cornerAngle <= 200) message=""; //182.99146692110685 is max ?? 192?? why?
        else message="błąd, zły kąt";
        if (!message.contentEquals("")) tts.speak(message, TextToSpeech.QUEUE_ADD, null);
    }
}
