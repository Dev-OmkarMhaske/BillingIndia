package com.tsysinfo.billing;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by administrator on 16/5/16.
 */
public class ConvertFile {

    public static void convertToText(String in, String out){

        Log.w("ConvertFile","In: "+in);
        Log.w("ConvertFile","Out "+out);

        FileInputStream instream = null;
        FileOutputStream outstream = null;

        try{
            File infile =new File(in);
            File outfile =new File(out);

            instream = new FileInputStream(infile);
            outstream = new FileOutputStream(outfile);

            byte[] buffer = new byte[1024];

            int length;
    	    /*copying the contents from input stream to
    	     * output stream using read and write methods
    	     */
            while ((length = instream.read(buffer)) > 0){
                outstream.write(buffer, 0, length);
            }

            //Closing the input/output file streams
            instream.close();
            outstream.close();

            System.out.println("File copied successfully!!");

        }catch(IOException ioe){
            ioe.printStackTrace();
        }

    }

}
