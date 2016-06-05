package com.gmail.maciekhtc.offroadmaps;

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
    public static void fileInit(String filePath)
    {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = br.readLine()) != null) {
                //Read line
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
    }
}
