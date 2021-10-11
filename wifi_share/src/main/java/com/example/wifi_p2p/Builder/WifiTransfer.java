package com.example.wifi_p2p.Builder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;

import com.example.wifi_p2p.Data.SharedPrefs;
import com.example.wifi_p2p.Enum.FileType;
import com.example.wifi_p2p.P2pUtil.P2pUtility;
import com.example.wifi_p2p.WiFiDirectActivity;

import java.io.File;

import static androidx.core.content.FileProvider.getUriForFile;
import static com.example.wifi_p2p.WiFiDirectActivity.SENDER_TAG;

public class WifiTransfer {
    public static final int FILE_PATH_REQUEST = 1111;


    public static class Builder {
        private Activity activity;
        private FileType transferType;
        private String json;
        private String jsonFileName;

        public Builder(FileType transferType, Activity activity) {
            this.transferType = transferType;
            this.activity = activity;

        }

        public Builder setJsonString(String json) {
            this.json = json;
            return this;
        }

        public Builder setJsonFileName(String jsonFileName) {
            this.jsonFileName = jsonFileName;
            return this;
        }

        public boolean canTransfer() throws IllegalArgumentException {
            if (TextUtils.isEmpty(json)) {
                throw new IllegalArgumentException("json string is not set! Please set json string to use this method.");
            }
            if (TextUtils.isEmpty(jsonFileName)) {
                throw new IllegalArgumentException("json file name path is not set! Please set json file name to use this method.");
            }
            return true;
        }


        public WifiTransfer build() {
            transferTypeSelected();
            return null;
        }

        private void transferTypeSelected() {
            String fileName = this.jsonFileName;
            String json = this.json;

            if (this.transferType == FileType.FILE) {
                SharedPrefs sharedPrefs = new SharedPrefs(activity);
                sharedPrefs.setKeyFileType(this.transferType);

                // Start Wifi Direct Activity
               transitionToWifiActivity(activity);

            } else if (this.transferType == FileType.TABLE) {
                    if (canTransfer()) {

                        // Store transfer type in shared preference
                        SharedPrefs sharedPrefs = new SharedPrefs(activity);
                        sharedPrefs.setKeyFileType(this.transferType);

                        // Convert json String to file
                        P2pUtility.jsonToFile(json, fileName, activity);

                        // Convert filePath to content Uri with File Provider and storing to shared pref
                        String storage_state = Environment.getExternalStorageState();
                        if (storage_state.equals(Environment.MEDIA_MOUNTED)) {
                            File dirs = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/BG-JSON-FILE", fileName + ".json");
                            if (!dirs.exists() && !dirs.mkdirs()) {
                                //Unable to create folder;
                                Log.d(SENDER_TAG, "build: Json File not Found");
                            } else {
                                Uri contentUri = getUriForFile(activity, "com.example.wifi_p2p.fileProvider", dirs);

                                // Store file path in shared preference
                                sharedPrefs.setKeyFilePath(contentUri.toString());
                            }
                        }

                        // Start Wifi Direct Activity
                       transitionToWifiActivity(activity);
                    }

            }
            else if (this.transferType == FileType.NONE){
                transitionToWifiActivity(activity);
            }
        }
    }

    // Intent to Wifi Share Activity
    public static void transitionToWifiActivity(Activity activity) {
        Intent intent = new Intent(activity, WiFiDirectActivity.class);
        activity.startActivityForResult(intent, FILE_PATH_REQUEST);
    }

}
