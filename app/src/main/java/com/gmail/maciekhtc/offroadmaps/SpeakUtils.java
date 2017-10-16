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
    private static final int pointsAbove = 2/*1*/, pointsBelow = 6/*5*/, speedDivider = 20/*20*/;

    public static void newPosition(int indexOfPoint, ArrayList<LatLng> currentLine, int speed)
    {
        try {
            boolean junctionFlag = false;       //todo search in next few points not distance (dist to junction == 0)
            if (indexOfPoint != -1 && indexOfPoint < currentLine.size()) {
                for (LatLng junction : PointUtils.junctionPoints) {
                    if (PointUtils.calculateDistance(currentLine.get(indexOfPoint), junction) < 10) {
                        roadCross();
                        junctionFlag = true;
                        break;
                    }
                }
                watchOut = junctionFlag;
            }
            if (!junctionFlag) {
                //
                //done todo calculate index using speed

                //done todo check multiple combination of points to calculate angles and compare them to choose the most narrow one
                if (currentLine == currentLineOld) {
                    if (indexOfPoint > indexOfPointOld) {   //moving to higher index
                        if (currentLine.size() - 1 >= indexOfPoint + pointsBelow + (speed / speedDivider)) {

                            int startIndex = indexOfPoint + pointsAbove + (speed / speedDivider);
                            int endIndex = indexOfPoint + pointsBelow + (speed / speedDivider);

                            double minAngle = 180;
                            double tempAngle;
                            for (int a = startIndex; a < endIndex; a++) {
                                for (int b = a; b < endIndex; b++) {
                                    tempAngle = PointUtils.calculateAngle(currentLine.get(startIndex),
                                            currentLine.get(a),
                                            currentLine.get(b));
                                    if (minAngle > tempAngle) minAngle = tempAngle;
                                }
                            }
                            corner(minAngle);

                            watchOut = false;
                        } else {
                            roadCross();    //end of line while moving forward
                        }
                    } else if (indexOfPoint < indexOfPointOld) {   //moving to lower index
                        if (0 <= indexOfPoint - (pointsBelow + (speed / speedDivider))) {

                            int startIndex = indexOfPoint - (pointsAbove + (speed / speedDivider));
                            int endIndex = indexOfPoint - (pointsBelow + (speed / speedDivider));

                            double minAngle = 180;
                            double tempAngle;
                            for (int a = startIndex; a > endIndex; a--) {
                                for (int b = a; b > endIndex; b--) {
                                    tempAngle = PointUtils.calculateAngle(currentLine.get(startIndex),
                                            currentLine.get(a),
                                            currentLine.get(b));
                                    if (minAngle > tempAngle) minAngle = tempAngle;
                                }
                            }
                            corner(minAngle);

                            /*
                            LatLng point1 = currentLine.get(indexOfPoint - (1 + (speed / 10)));
                            LatLng point2 = currentLine.get(indexOfPoint - (3 + (speed / 10)));
                            LatLng point3 = currentLine.get(indexOfPoint - (5 + (speed / 10)));
                            //Log.d("OffroadMap", "moving to lower index");
                            corner(PointUtils.calculateAngle(point1, point2, point3));*/
                            watchOut = false;
                        } else {
                            roadCross();    //end of line while moving backwards
                        }
                    } else {   //standing still
                    }
                } else {   //change line, road cross?
                    //Log.d("OffroadMap", "Line change");
                    roadCross();
                }
            }
            //update positiond
            currentLineOld = currentLine;
            indexOfPointOld = indexOfPoint;
        }
        catch (Exception e)
        {
            e.printStackTrace();    //encountered one unexpected behaviour, happened only one time,
                                    // app was closed, dont know if crashed or cleaned from memory, will be investigated
        }
    }

    private static void roadCross() {
        if (!watchOut)
        {
            //tts.speak("Uważaj!", TextToSpeech.QUEUE_ADD, null);
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



        //todo stopien ciasnosci na bazie predkosci? chyba glupie
        //
        if (cornerAngle < 5) message = "";  ///////should not be used
        else if (cornerAngle < 70) message+="1";
        else if (cornerAngle < 110) message+="2";
        else if (cornerAngle < 135) message+="3";
        else if (cornerAngle < 150) message+="4";
        else if (cornerAngle < 160) message+="5";
        else if (cornerAngle < 170) message+="6";
        else if (cornerAngle <= 180) message="";    //straight
        else message="";   //not needed ?
        if (!cornerMessage.contentEquals("")) watchOut=false;
        String messageToSay = message;
        if (!cornerMessage.contentEquals("")&&!message.contentEquals(""))
        {
            if ((cornerMessage.contains("prawy") && message.contains("prawy")) || (cornerMessage.contains("lewy") && message.contains("lewy"))) {
                try {
                    int lastNumber = Integer.parseInt(cornerMessage.split(" ")[1]);
                    int newNumber = Integer.parseInt(message.split(" ")[1]);
                    if (lastNumber <= newNumber) messageToSay = "długi";        //todo: change to == ? to say every angle change not "długi"
                    else messageToSay = "do " + newNumber;//narrow corner

                } catch (Exception e) { }
            }
            else messageToSay = "do " + messageToSay;   //different corner direction
        }
        messageToSay = messageToSay.replaceAll("1", "jeden");
        messageToSay = messageToSay.replaceAll("2", "dwa");
        messageToSay = messageToSay.replaceAll("3", "trzy");
        messageToSay = messageToSay.replaceAll("4", "cztery");
        messageToSay = messageToSay.replaceAll("5", "pięć");
        messageToSay = messageToSay.replaceAll("6", "sześć");
        if (!message.contentEquals("")) tts.speak(messageToSay, TextToSpeech.QUEUE_ADD, null);
        cornerMessage = message;
    }
}
