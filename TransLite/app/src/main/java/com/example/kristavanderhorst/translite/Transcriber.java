package com.example.kristavanderhorst.translite;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Krista Vanderhorst on 4/15/2018.
 */

public class Transcriber {
    private static Transcriber mInstance;
    private static Context mCtx;
    private FileWriter mFileWriter;

    private Transcriber(Context context) {
        mCtx = context;

        String pth = Environment.getExternalStorageDirectory() + File.separator + "TransLite";
        File folder = new File(pth);

        if (!folder.exists())
            folder.mkdirs();

        Date time = Calendar.getInstance().getTime();
        File f = new File(folder.toString()+File.separator+"transcribe_"+time.toString()+".txt");

        try {
            mFileWriter = new FileWriter(f);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static synchronized Transcriber getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Transcriber(context);
        }
        return mInstance;
    }

    public void write(String usr, String txt) {
        try {
            Date time = Calendar.getInstance().getTime();
            String dataline = "[" + time.toString() + "] " + usr + ": " + txt + "\n";
            mFileWriter.append(dataline);
            mFileWriter.flush();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
