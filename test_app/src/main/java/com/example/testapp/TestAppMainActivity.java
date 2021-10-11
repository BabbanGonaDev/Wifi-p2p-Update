package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.testapp.RoomDb.Data;
import com.example.testapp.RoomDb.Repository.DataRepository;
import com.example.testapp.SharedPrefs.SharedPrefs;
import com.example.wifi_p2p.Builder.WifiTransfer;
import com.example.wifi_p2p.Enum.FileType;
import com.example.wifi_p2p.P2pUtil.P2pUtility;
import com.example.wifi_p2p.WiFiDirectActivity;
import com.google.gson.Gson;

import java.util.List;

import static com.example.wifi_p2p.Builder.WifiTransfer.FILE_PATH_REQUEST;

public class TestAppMainActivity extends AppCompatActivity {
    private Button btn_share_file, btn_share_db, btn_receive;
    private List<Data> allData;
    com.example.testapp.SharedPrefs.SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__main_testapp);

        btn_share_file = findViewById(R.id.btn_share_file);
        btn_share_db = findViewById(R.id.btn_share_db);
        btn_receive = findViewById(R.id.btn_receive);
        sharedPrefs = new SharedPrefs(TestAppMainActivity.this);


        btn_share_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiTransfer shareFile = new WifiTransfer.Builder(FileType.FILE, TestAppMainActivity.this).build();


            }
        });

        btn_share_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Convert Database table to string
                dbToString();

                // Receive db json file stored in shared preference
                String json = sharedPrefs.getKeyJson();

                // start Wifi direct Module
                WifiTransfer shareDb = new WifiTransfer.
                        Builder(FileType.TABLE, TestAppMainActivity.this)
                        .setJsonString(json)
                                .setJsonFileName("Contact").build();
            }
        });

        btn_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             WifiTransfer receive = new WifiTransfer.Builder(FileType.NONE, TestAppMainActivity.this).build();
            }
        });

//        // Method to convert and received database file path to Json string
//        String filePath = sharedPrefs.getKeyJsonFilePath();
//        try {
//            P2pUtility.fileToJson(filePath, TestAppMainActivity.this);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    private void dbToString() {
        new DataRepository(getApplication()).getAllDatas().observe(TestAppMainActivity.this, new Observer<List<Data>>() {
            @Override
            public void onChanged(List<Data> datas) {
                if (datas != null) {
                    allData = datas;

                    // Convert Java Object to Json
                    Gson gson = new Gson();
                    String json = gson.toJson(allData);
                    sharedPrefs.setKeyJson(json);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PATH_REQUEST) {
            if (resultCode == RESULT_OK) {
                String receivedPath = data.getStringExtra(WiFiDirectActivity.EXTRA_DATA_PATH);
                Toast.makeText(this, "I have received " + receivedPath, Toast.LENGTH_LONG).show();

                // save receivedPath to shared preference.
                sharedPrefs.setKeyJsonFilePath(receivedPath);


            }
        }
    }
}