package com.gmail.maciekhtc.offroadmaps;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by 15936 on 05.06.2016.
 */
public class FileUtils {
    public static String filePath = Environment.getExternalStorageDirectory() + "/OffroadMap/";

    public static ArrayList<String> fileInit() {
        ArrayList<String> listString = new ArrayList();
        try {
            BufferedReader settingsBr = new BufferedReader(new FileReader(filePath + "Settings.txt"));
            String line = "";
            while ((line = settingsBr.readLine()) != null) {
                //Read line
                if (line.startsWith("username:")) {
                    Settings.username = line.split(":")[1];
                } else if (line.startsWith("group:")) {
                    Settings.group = line.split(":")[1];
                } else if (line.startsWith("followMyPosition:")) {
                    if (line.split(":")[1].contentEquals("true") || line.split(":")[1].contentEquals("1"))
                        Settings.followMyPosition = true;
                    else Settings.followMyPosition = false;
                } else if (line.startsWith("saveNewPoints:")) {
                    if (line.split(":")[1].contentEquals("true") || line.split(":")[1].contentEquals("1"))
                        Settings.saveNewPoints = true;
                    else Settings.saveNewPoints = false;
                } else if (line.startsWith("updateOnline:")) {
                    if (line.split(":")[1].contentEquals("true") || line.split(":")[1].contentEquals("1"))
                        Settings.updateOnline = true;
                    else Settings.updateOnline = false;
                } else if (line.startsWith("speakMessages:")) {
                    if (line.split(":")[1].contentEquals("true") || line.split(":")[1].contentEquals("1"))
                        Settings.speakMessages = true;
                    else Settings.speakMessages = false;
                } else if (line.startsWith("speakCorners:")) {
                    if (line.split(":")[1].contentEquals("true") || line.split(":")[1].contentEquals("1"))
                        Settings.speakCorners = true;
                    else Settings.speakCorners = false;
                }
            }
        } catch (FileNotFoundException e1) {
            //No file
            //Log.d("OffroadMap", "No settings file");
            try {
                new File(filePath).mkdirs();
                FileWriter fileWriterSettings = new FileWriter(filePath + "Settings.txt", true);
                //Log.d("OffroadMap", "File settings created");
                fileWriterSettings.write("\r\n");
                fileWriterSettings.close();
            } catch (IOException e) {
                //IOException
                //Log.d("OffroadMap", "Can not create file");
                e.printStackTrace();
            }
        } catch (IOException e) {
            //IOException
            //Log.d("OffroadMap", "Can not read file");
            e.printStackTrace();
        }
        //
        try {
            BufferedReader mapBr = new BufferedReader(new FileReader(filePath + "Map.txt"));
            String line = "";
            while ((line = mapBr.readLine()) != null) {
                if (!line.startsWith("#")) {
                    listString.add(new String(line));

                }
            }
            //Log.d("OffroadMap", "Files loaded");
        } catch (FileNotFoundException e1) {
            //No file Map.txt
            //Log.d("OffroadMap", "No map file");
            try {
                BufferedReader mapBr = new BufferedReader(new FileReader(filePath + "MapTemp.txt"));
                String line = "";
                while ((line = mapBr.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        listString.add(new String(line));

                    }
                }
                //Log.d("OffroadMap", "Files loaded");
            }   catch (FileNotFoundException e2) {
                //No file MapTemp.txt too
                try {
                    new File(filePath).mkdirs();
                    FileWriter fileWriter = new FileWriter(filePath + "Map.txt", true);
                    //Log.d("OffroadMap", "File map created");
                    fileWriter.write("#Offroad Map points list, you can share this list with others\r\n");
                    fileWriter.close();
                    //Log.d("OffroadMap", "File header write ended");
                } catch (IOException e) {
                    //IOException
                    //Log.d("OffroadMap", "Can not create file");
                    e.printStackTrace();
                }

            } catch (IOException e) {
                //IOException
                e.printStackTrace();
            }
        } catch (IOException e) {
            //IOException
            //Log.d("OffroadMap", "Can not read file");
            e.printStackTrace();
        }
        return listString;
    }

    public static void fileWriteLines() {
        try {
            FileWriter fileWriter = new FileWriter(filePath + "MapTemp.txt", false);
            //Log.d("OffroadMap", "File opened for append");
            fileWriter.write("#Offroad Map points list, you can share this list with others" + "\r\n");
            for (String line : PointUtils.savePoints()) {
                fileWriter.write(line + "\r\n");
            }
            fileWriter.close();
            File newFile = new File(filePath + "Map.txt");
            File oldFile = new File(filePath + "MapTemp.txt");
            if (newFile.exists())
                newFile.delete();
            oldFile.renameTo(newFile);
            Log.d("OffroadMap", "File filled with new lines");
        } catch (IOException e) {
            //IOException
            //Log.d("OffroadMap", "Can not append to file");
            e.printStackTrace();
        }
    }

    public static void fileWriteSettings() {
        try {
            FileWriter fileWriter = new FileWriter(filePath + "Settings.txt", false);
            fileWriter.write("username:" + Settings.username + "\r\n");
            fileWriter.write("group:" + Settings.group + "\r\n");
            fileWriter.write("followMyPosition:" + Settings.followMyPosition + "\r\n");
            fileWriter.write("saveNewPoints:" + Settings.saveNewPoints + "\r\n");
            fileWriter.write("updateOnline:" + Settings.updateOnline + "\r\n");
            fileWriter.write("speakMessages:" + Settings.speakMessages + "\r\n");
            fileWriter.write("speakCorners:" + Settings.speakCorners + "\r\n");
            fileWriter.close();
            //Log.d("OffroadMap", "Settings saved");
        } catch (IOException e) {
            //IOException
            //Log.d("OffroadMap", "Can not save settings");
            e.printStackTrace();
        }
    }
}
