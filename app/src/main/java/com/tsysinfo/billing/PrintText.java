package com.tsysinfo.billing;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by administrator on 5/5/16.
 */
public class PrintText {

    public static void appendLog(String text)
    {
        String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/print/";
        File folder = new File(extStorageDirectory);
        folder.mkdir();

        File logFile = new File(extStorageDirectory+"print.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            /*PrintWriter writer = new PrintWriter(logFile);
            writer.print("");
            writer.print(text);*/

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, false));
            buf.write(text);
           // buf.append(text);
           // buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
