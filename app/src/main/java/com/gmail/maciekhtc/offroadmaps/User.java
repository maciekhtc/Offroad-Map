package com.gmail.maciekhtc.offroadmaps;

import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;
import java.util.Random;

/**
 * Created by 15936 on 12.06.2016.
 */
public class User {
    public long lastTime;
    public String username;
    public double lat;
    public double lon;
    public Marker marker = null;
    public String message = null;
    private boolean say = false;
    private boolean toDelete = false;
    //

    public User (String lastTime, String username, String lat, String lon, String msg) {
        this.lastTime = Long.parseLong(lastTime);
        this.username = Uri.decode(username);
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        if (!msg.contentEquals("empty"))
        {
            if (message == null) this.say = true;
            this.message = Uri.decode(msg);
        }
        else this.message = null;
        MapUtils.toUpdate.add(this);
    }
    public void setParams (String lastTime, String username, String lat, String lon, String msg) {
        this.lastTime = Long.parseLong(lastTime);
        this.username = username;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        if (!msg.contentEquals("empty"))
        {
            if (message == null) this.say = true;
            this.message = Uri.decode(msg);
        }
        else this.message = null;
        MapUtils.toUpdate.add(this);
    }

    public LatLng getLatLng() {
        return new LatLng(lat,lon);
    }

    public void updateMarker(){
        if (toDelete)
        {
            //toDelete == true so i can speak about user disconnected
            if (Settings.speakMessages) SpeakUtils.tts.speak("Znikł: "+username, TextToSpeech.QUEUE_ADD, null);
            this.marker.setVisible(false);
            this.marker.remove();
            this.marker = null;
            Log.d("OffroadMap", "Removed online user " + username);
            return;
        }
        if (marker != null)
        {
            if (message == null) {
                marker.setVisible(false);
                marker.remove();
                marker = MapUtils.mMap.addMarker(new MarkerOptions()
                        .position(getLatLng())
                        .title(username));
            }
            else
            {
                marker.setPosition(getLatLng());
                marker.setTitle(username);
            }
        }
        else
        {
            //marker == null so now i can speak about user connected
            if (Settings.speakMessages) SpeakUtils.tts.speak("Dołączył: "+username, TextToSpeech.QUEUE_ADD, null);
            Log.d("OffroadMap", "Added online user " + username);
            marker = MapUtils.mMap.addMarker(new MarkerOptions()
                    .position(getLatLng())
                    .title(username));
        }

        if (message!=null)
        {
            marker.setSnippet(message);
            if (say && Settings.speakMessages)
            {
                SpeakUtils.tts.speak(username + ":" + message, TextToSpeech.QUEUE_ADD, null);
                say = false;
            }
            marker.showInfoWindow();
        }
        else
        {
            marker.hideInfoWindow();
            marker.setSnippet(null);
        }
    }
    public void deleteUser()
    {
        toDelete = true;
        MapUtils.toUpdate.add(this);
    }
}

