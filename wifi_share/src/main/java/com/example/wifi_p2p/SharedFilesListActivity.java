package com.example.wifi_p2p;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SharedFilesListActivity extends AppCompatActivity {
    private ListView listView;
    private List<String> fileNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_files_list);

        // Initializing view
        listView = findViewById(R.id.listview);

        // Set Actionbar title
        getSupportActionBar().setTitle("DOWNLOADS");

        // Retrieve folder path and display all files in directory
        String path = Environment.getExternalStorageDirectory().toString() + "/Download/BabbanGona";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);

        File[] files = directory.listFiles();
        if (files != null) {
            Log.d("Files", "Size: " + files.length);
            for (int i = 0; i < files.length; i++) {
                Log.d("Files", "FileName:" + files[i].getName());
                fileNameList.add(files[i].getName());

            }

            // set up list view Adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, fileNameList);
            listView.setAdapter(adapter);
        }
    }
}