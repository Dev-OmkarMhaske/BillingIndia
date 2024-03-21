package com.tsysinfo.billing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by administrator on 5/5/16.
 */
public class LogError {

    public void appendLog(String text)
    {
        File logFile = new File("sdcard/log.file");
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
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String strDate = sdf.format(c.getTime());

            //BufferedWriter for performance, true to set append to file

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text +"  "+ strDate+ "\n");
            buf.newLine();
            buf.flush();
            buf.close();

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
