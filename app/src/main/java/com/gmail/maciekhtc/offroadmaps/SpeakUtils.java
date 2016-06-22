package com.gmail.maciekhtc.offroadmaps;

import android.speech.tts.TextToSpeech;

import com.google.android.gms.maps.model.LatLng;

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
                try
                {
                    LatLng point1=currentLine.get(indexOfPoint);
                    LatLng point2=currentLine.get(indexOfPoint+2);
                    LatLng point3=currentLine.get(indexOfPoint+4);
                    watchOut = false;
                    //calculateAngle(point1,point2,point3);
                } catch (Exception e)
                {
                    roadCross();
                }
            }
            else if (indexOfPoint < indexOfPointOld)
            {   //moving to lower index
                try
                {
                    LatLng point1=currentLine.get(indexOfPoint);
                    LatLng point2=currentLine.get(indexOfPoint-2);
                    LatLng point3=currentLine.get(indexOfPoint-4);
                    watchOut = false;
                    //calculateAngle(point1,point2,point3);
                } catch (Exception e)
                {
                    roadCross();
                }
            }
            else
            {   //standing still

            }
        }
        else
        {   //change line, road cross?
            roadCross();
        }
        //update positiond
        currentLineOld = currentLine;
        indexOfPointOld = indexOfPoint;
    }

    private static double calculateAngle(LatLng point1, LatLng point2, LatLng point3) {

        return 0;
    }

    private static void roadCross() {
        if (!watchOut)
        {
            watchOut = true;
            tts.speak("Uważaj!", TextToSpeech.QUEUE_ADD, null);
        }
    }
}
