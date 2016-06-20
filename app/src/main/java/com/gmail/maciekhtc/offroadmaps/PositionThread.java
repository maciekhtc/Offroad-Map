package com.gmail.maciekhtc.offroadmaps;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.iid.InstanceID;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by 15936 on 12.06.2016.
 */
public class PositionThread extends Thread {
    //
    public double myLat = 0;
    public double myLon = 0;
    public String users = "";
    //
    private URL url;
    private HttpURLConnection urlConnection = null;
    private String inLine;
    private String deviceId;
    public boolean running = true;

    private String myMessage = "empty";
    private int messageRepeat = 0;

    public void setDeviceId(String id) {
        deviceId = "User" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10 + id;
    }

    //
    public void run() {
        while (true) {
            if (myLat != 0) {
                try {
                    if (messageRepeat > 0) {
                        messageRepeat--;
                    } else {
                        myMessage = "empty";
                    }
                    url = new URL(generateRequestUrl());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    users = "";
                    while ((inLine = in.readLine()) != null) {
                        users += inLine;
                    }
                    updateUsers();
                } catch (Exception e) {
                } finally {
                    urlConnection.disconnect();
                }
            }
            try {
                Thread.sleep(3000); //wait 3 sec each refresh
                if (!running) break;        //exit statement
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateRequestUrl() {
        return "http://student.pwsz.elblag.pl/~15936/OffroadMap/getUsers.php" +
                "?deviceId=" + deviceId +
                "&username=" + Settings.username +
                "&lat=" + myLat +
                "&lon=" + myLon +
                "&group=" + Settings.group +
                "&msg=" + Uri.encode(myMessage) + ":";
        //add message
    }

    private void updateUsers() {
        for (String userLine : users.split("<br/>")) {
            String userParams[] = userLine.split(":");
            if (MapUtils.userList.get(userParams[0]) != null)   //0 - unique id
            {
                MapUtils.userList.get(userParams[0]).setParams(userParams[1], userParams[2], userParams[3], userParams[4], userParams[6]);
            } else if (userLine.contains(":")) {
                MapUtils.userList.put(userParams[0], new User(userParams[1], userParams[2], userParams[3], userParams[4], userParams[6]));
            }
        }

    }

    public void setMyMessage(String msg) {
        myMessage = msg;
        messageRepeat = 5;
    }
}
