package com.gmail.maciekhtc.offroadmaps;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 15936 on 12.06.2016.
 */
public class PositionThread extends Thread {
    //
    public double myLat = 0;
    public double myLon = 0;
    public String username = "";
    public String users = "";
    //
    private URL url;
    private HttpURLConnection urlConnection = null;
    private String inLine;
    private String deviceId = "User" +
            Build.BOARD.length()%10+ Build.BRAND.length()%10 +
            Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
            Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
            Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
            Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
            Build.TAGS.length()%10 + Build.TYPE.length()%10 +
            Build.USER.length()%10 ;
    //
    public void run() {
        while (true) {
            if (myLat != 0)
            {
                try {
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
                try {
                    Thread.sleep(5000); //wait 5 sec each refresh
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    private String generateRequestUrl() {
        return "http://student.pwsz.elblag.pl/~15936/OffroadMap/getUsers.php"+
                "?deviceId="+deviceId+
                "&username="+username+
                "&lat="+myLat+
                "&lon="+myLon;
    }
    private void updateUsers() {
        for (String userLine : users.split("<br/>"))
        {
            String []userParams = userLine.split(":");
            if (MapUtils.userList.containsKey(userParams[0]))   //0 - unique id
            {
                Log.d("OffroadMap", "Online User updated");
                MapUtils.userList.get(userParams[0]).setParams(userParams[1],userParams[2],userParams[3],userParams[4]);
            }
            else if (userLine.contains(":")) {
                Log.d("OffroadMap", "Online User created");
                MapUtils.userList.put(userParams[0],new User(userParams[1],userParams[2],userParams[3],userParams[4]));
            }
        }

    }
}
