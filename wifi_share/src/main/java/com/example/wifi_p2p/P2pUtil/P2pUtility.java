package com.example.wifi_p2p.P2pUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.example.wifi_p2p.Builder.WifiTransfer;
import com.example.wifi_p2p.WiFiDirectActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;



public class P2pUtility {


    private static final int RECEIVED_FILE_PATH_REQUEST = 1 ;

    // Write Json  string to Json file
    public static void jsonToFile(String json, String fileName, Context context) {

        String storage_state = Environment.getExternalStorageState();
        if (storage_state.equals(Environment.MEDIA_MOUNTED)) {

            File dirs = new File(context.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/BG-JSON-FILE");
            if (!dirs.exists() && !dirs.mkdirs()) {
                //Unable to create folder;
            } else {
                File json_file = new File(dirs.getPath(), fileName + ".json");
                try {
                    FileOutputStream fOut = new FileOutputStream(json_file);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(fOut);
                    outputWriter.write(json);
                    outputWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Convert Json file to Json string
    public static String fileToJson(String filePath, Context context) throws Exception {
        StringBuilder sb = new StringBuilder();

                File json_file = new File(filePath);
                if (json_file.exists()) {
                    FileInputStream fis = new FileInputStream(json_file);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(isr);

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT).show();
                }
        return sb.toString();
    }

}
