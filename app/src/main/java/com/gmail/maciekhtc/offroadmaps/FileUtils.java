package com.gmail.maciekhtc.offroadmaps;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by 15936 on 05.06.2016.
 */
public class FileUtils {
    private static String filePath = Environment.getExternalStorageDirectory() + "/OffroadMapCoordinates" + ".txt";

    public static ArrayList<String> fileInit()
    {
        ArrayList<String> listString = new ArrayList();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = br.readLine()) != null) {
                //Read line
                if (!line.startsWith("#"))
                {
                    listString.add(new String(line));
                }
                Log.d("OffroadMap", "Read line");
            }
            Log.d("OffroadMap","File loaded");
        } catch (FileNotFoundException e1) {
            //No file
            Log.d("OffroadMap", "No file");
            try {
                FileWriter fileWriter = new FileWriter(filePath, true);
                Log.d("OffroadMap", "File created");
                fileWriter.write("#Offroad Map points list, you can share this list with others, even small fragments");
                fileWriter.close();
                Log.d("OffroadMap", "File header write ended");
            } catch (IOException e) {
                //IOException
                Log.d("OffroadMap", "Can not create file");
                e.printStackTrace();
            }
        } catch (IOException e) {
            //IOException
            Log.d("OffroadMap", "Can not read file");
            e.printStackTrace();
        }
        return listString;
    }

    public static void fileWriteLines(ArrayList<String> newLines) {
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            Log.d("OffroadMap", "File opened for append");
            for (String line: newLines)
            {
                fileWriter.write(line);
            }
            fileWriter.close();
            Log.d("OffroadMap", "File filled with new lines");
        } catch (IOException e) {
            //IOException
            Log.d("OffroadMap", "Can not append to file");
            e.printStackTrace();
        }
    }
}
