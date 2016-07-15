package com.gmail.maciekhtc.offroadmaps;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private PositionThread positionThread = null;
    RelativeLayout standardOverlay;
    RelativeLayout settingsOverlay;
    EditText usernameText;
    EditText groupText;
    CheckBox followMyPositionCheckBox;
    CheckBox saveNewPointsCheckBox;
    CheckBox updateOnlineCheckBox;
    CheckBox speakMessagesCheckBox;
    CheckBox speakCornersCheckBox;
    boolean linesDrawn = false;
    Polyline currentLine = null;
    LinkedList<LatLng> currentLinePoints = null;
    boolean gpsEnabled = false;
//http://student.pwsz.elblag.pl/~15936/OffroadMap/getUsers.php?deviceId=User32323211dsf&username=inny&lat=54.1752883&lon=19.4068716&group=fornewones&msg=empty

    @Override
    public void onBackPressed() {                                                           //zabij proces przy wylaczaniu aplikacji klawiszem back
        if (settingsOverlay.isShown()) {
            settingsOverlay.setVisibility(View.GONE);
            standardOverlay.setVisibility(View.VISIBLE);
        } else {
            positionThread.running = false;
            FileUtils.fileWriteSettings();
            FileUtils.fileWriteLines();
            super.onBackPressed();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //
        //

        standardOverlay = (RelativeLayout) findViewById(R.id.standardOverlay);
        settingsOverlay = (RelativeLayout) findViewById(R.id.settingsOverlay);
        usernameText = (EditText) findViewById(R.id.usernameText);
        groupText = (EditText) findViewById(R.id.groupText);
        followMyPositionCheckBox = (CheckBox) findViewById(R.id.followMyPositionCheckBox);
        saveNewPointsCheckBox = (CheckBox) findViewById(R.id.saveNewPointsCheckBox);
        updateOnlineCheckBox = (CheckBox) findViewById(R.id.updateOnlineCheckBox);
        speakMessagesCheckBox = (CheckBox) findViewById(R.id.speakMessagesCheckBox);
        speakCornersCheckBox = (CheckBox) findViewById(R.id.speakCornersCheckBox);

        Button closeSettingsButton = (Button) findViewById(R.id.closeSettingsButton);
        closeSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsOverlay.setVisibility(View.GONE);
                standardOverlay.setVisibility(View.VISIBLE);
                View view = getWindow().findViewById(R.id.settingsOverlay);
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        Button settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                standardOverlay.setVisibility(View.GONE);
                settingsOverlay.setVisibility(View.VISIBLE);
            }
        });
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save
                saveSettings();
                settingsOverlay.setVisibility(View.GONE);
                standardOverlay.setVisibility(View.VISIBLE);
                View view = getWindow().findViewById(R.id.settingsOverlay);
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        Button messageButton = (Button) findViewById(R.id.messageButton);
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //message
                promptSpeechInput();
            }
        });


        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        positionThread = new PositionThread();
        positionThread.setDeviceId(InstanceID.getInstance(getApplicationContext()).getId());
        positionThread.start();

        PointUtils.getLines(PointUtils.pointsFromFile(FileUtils.fileInit()));
        loadSettings();
        //
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("OffroadMap","No PERMISSIONS");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1500, 0, this);
        //
        SpeakUtils.tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    SpeakUtils.tts.setLanguage(Locale.getDefault());
                    if (Settings.speakMessages)
                    {
                        //SpeakUtils.tts.speak("Witaj "+Settings.username,TextToSpeech.QUEUE_ADD,null);
                        //SpeakUtils.tts.speak("Witaj "+Settings.username,TextToSpeech.QUEUE_ADD,Bundle.EMPTY,"message");
                    }
                }
            }
        });
    }

    private void saveSettings() {
        Settings.username = usernameText.getText().toString();
        Settings.group = groupText.getText().toString();
        Settings.followMyPosition = followMyPositionCheckBox.isChecked();
        Settings.saveNewPoints = saveNewPointsCheckBox.isChecked();
        Settings.updateOnline = updateOnlineCheckBox.isChecked();
        Settings.speakMessages = speakMessagesCheckBox.isChecked();
        Settings.speakCorners = speakCornersCheckBox.isChecked();
        //Log.d("OffroadMap", "Settings saved");
    }

    private void loadSettings() {
        usernameText.setText(Settings.username);
        groupText.setText(Settings.group);
        followMyPositionCheckBox.setChecked(Settings.followMyPosition);
        saveNewPointsCheckBox.setChecked(Settings.saveNewPoints);
        updateOnlineCheckBox.setChecked(Settings.updateOnline);
        speakMessagesCheckBox.setChecked(Settings.speakMessages);
        speakCornersCheckBox.setChecked(Settings.speakCorners);
        //Log.d("OffroadMap", "Settings loaded");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapUtils.mMap = this.mMap;
        //
        mMap.setMyLocationEnabled(true);
        //
        currentLine=mMap.addPolyline(new PolylineOptions().color(Color.RED).width(2.5f));
        currentLinePoints = new LinkedList();
        //
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 150, null);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (!gpsEnabled)locationChange(location);                  //disable location from map if gps provider is able to detect location
            }
        });
    }
    private void locationChange(Location location)
    {
        if (Settings.followMyPosition)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(MapUtils.latlngFromLocation(location)));
        if (location.getAccuracy() < 20) {
            if (Settings.saveNewPoints || Settings.speakCorners) {
                PointUtils.processNewPoint(location);   //add point to newPoints list which will be saved to the file
            }
            currentLinePoints.add(MapUtils.latlngFromLocation(location)); //add current point to the  list of red line
            currentLine.setPoints(currentLinePoints);   //draw red line from points
        }
        //Log.d("OffroadMap", "New Location accuracy: "+location.getAccuracy());
        if (Settings.updateOnline) {
            try {
                positionThread.myLat = location.getLatitude();
                positionThread.myLon = location.getLongitude();
            } catch (ConcurrentModificationException e)
            {
                e.printStackTrace();
            }
            MapUtils.updateOnlineUsers();   //update marker positions from main thread (not positionthread)
        }
        if (PointUtils.lines != null && !linesDrawn)
            drawLines();  //draw lines on map when not drawn and ready (lines not null)
    }
    private void drawLines()
    {
        linesDrawn = true;
        for (LinkedList<LatLng> line:PointUtils.lines)
        {
            mMap.addPolyline(new PolylineOptions().addAll(line).color(Color.WHITE).width(2.0f));
        }
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setText("98789");                                         //small integration with tasker
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,Locale.getDefault());//new Locale("Polish","Poland"));// //todo
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say the Message");
        try {
            startActivityForResult(intent, 100);    //??
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),"Speech not supported", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    positionThread.setMyMessage(result.get(0));
                    Log.d("OffroadMap",result.get(0));
                }
                break;
            }

        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setText("");
    }

    @Override
    public void onLocationChanged(Location location) {
        gpsEnabled = true;
        if (gpsEnabled)locationChange(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.contentEquals(LocationManager.GPS_PROVIDER)) gpsEnabled = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.contentEquals(LocationManager.GPS_PROVIDER)) gpsEnabled = false;
    }

    @Override
    protected void onStop() {
        positionThread.running = false;
        super.onStop();
    }
}
