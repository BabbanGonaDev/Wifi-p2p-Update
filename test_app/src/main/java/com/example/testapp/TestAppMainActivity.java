package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.testapp.RoomDb.Data;
import com.example.testapp.SharedPrefs.SharedPrefs;
import com.example.testapp.ViewModel.ViewModel;
import com.example.wifi_p2p.Builder.WifiTransfer;
import com.example.wifi_p2p.Enum.FileType;
import com.example.wifi_p2p.WiFiDirectActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.example.wifi_p2p.Builder.WifiTransfer.FILE_PATH_REQUEST;

public class TestAppMainActivity extends AppCompatActivity {
    private Button btn_share_file, btn_share_db, btn_receive;
    private List<Data> allData;
    private ViewModel viewModel;
    com.example.testapp.SharedPrefs.SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__main_testapp);

        btn_share_file = findViewById(R.id.btn_share_file);
        btn_share_db = findViewById(R.id.btn_share_db);
        btn_receive = findViewById(R.id.btn_receive);
        sharedPrefs = new SharedPrefs(TestAppMainActivity.this);
        viewModel = new ViewModelProvider(this).get(ViewModel.class);


        btn_share_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiTransfer shareFile = new WifiTransfer.Builder(FileType.FILE, TestAppMainActivity.this).build();


            }
        });

        btn_share_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String json = createJson();
//                allData = viewModel.getAllDatas();
//                if (allData != null) {
//                    // Convert Java Object to Json
//                    Gson gson = new Gson();
//                    String jsonString = gson.toJson(allData);
//                    displayToast(jsonString);
//                }

                // start Wifi direct Module
            WifiTransfer shareDb = new WifiTransfer.
                    Builder(FileType.TABLE, TestAppMainActivity.this)
                    .setJsonString(json)
                    .setJsonFileName("Contact").build();
        }
    });

        btn_receive.setOnClickListener(new View.OnClickListener()

    {
        @Override
        public void onClick (View v){
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

    private String createJson() {
        JSONObject person1 = new JSONObject();
        try {
            person1.put("Name", "Frank Lampard");
            person1.put("Age", "35");
            person1.put("Country", "England");
            person1.put("Occupation", "Footballer");
            person1.put("Sex", "Male");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject person2 = new JSONObject();
        try {
            person2.put("Name", "Uju Ejiofor");
            person2.put("Age", "26");
            person2.put("Country", "Nigerian");
            person2.put("Occupation", "Software Developer");
            person2.put("Sex", "Female");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        String p1 = person1.toString();
        String p2 = person2.toString();

        JsonArray people = new JsonArray();
        people.add(p1);
        people.add(p2);

        Gson gson = new Gson();
        String json = gson.toJson(people);
        return json;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PATH_REQUEST) {
            if (resultCode == RESULT_OK) {
                String receivedPath = data.getStringExtra(WiFiDirectActivity.EXTRA_DATA_PATH);
                Toast.makeText(this, "I have received " + receivedPath, Toast.LENGTH_LONG).show();

            }
        }
    }
}