package com.example.wifi_p2p.AsyncTask;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.wifi_p2p.Data.SharedPrefs;
import com.example.wifi_p2p.Model.DataModel;
import com.example.wifi_p2p.R;
import com.example.wifi_p2p.SharedFilesListActivity;
import com.example.wifi_p2p.WiFiDirectActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.example.wifi_p2p.WiFiDirectActivity.RECEIVER_TAG;

// Server thread
public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {
    private Context context;
    WiFiDirectActivity activity = new WiFiDirectActivity();
    public AsyncResponse delegate;


    public FileServerAsyncTask(Context context, AsyncResponse delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }


    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            ServerSocket serverSocket = new ServerSocket(8988);
            Log.d(RECEIVER_TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();
            Log.d(RECEIVER_TAG, "Server: connection done");
            InputStream inputstream = client.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inputstream);
            DataModel dataModel;
            dataModel = (DataModel) ois.readObject();

            String storage_state = Environment.getExternalStorageState();
            if (storage_state.equals(Environment.MEDIA_MOUNTED)) {
                String fileName = dataModel.getFileName();
                Long actualFileLength = dataModel.getFileLength();

                final File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/BabbanGona"),
                        fileName);
                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    Log.d(RECEIVER_TAG, "Directory doesn't exist so we are creating the folder");
                dirs.mkdirs();
                f.createNewFile();

               activity.copyReceivedFile(inputstream, new FileOutputStream(f), actualFileLength);
                ois.close();
                serverSocket.close();

                Log.d(RECEIVER_TAG, "receiver: Files copied and socket closed");

                Log.d(RECEIVER_TAG, "Received Path: " + f.getAbsolutePath());
                return f.getAbsolutePath();
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.e(RECEIVER_TAG, e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Toast.makeText(context.getApplicationContext(), R.string.file_received, Toast.LENGTH_SHORT).show();
            delegate.processFinish(result);

        }
    }
}