package com.example.wifi_p2p.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.wifi_p2p.Enum.FileType;

public  class SharedPrefs {

    private static final String KEY_RECV_FILE_ABS_PATH = "received_file_abs_path";
    private static final String KEY_RECV_FILE_TYPE = "received_file_type";
    private static final String KEY_RECV_DB_JSON = "received_db_json";
    public static final String SHARED_PREFS = "shared_prefs";
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefs(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

    }

    public void setKeyFilePath(String path) {
        editor.putString(KEY_RECV_FILE_ABS_PATH, path);
        editor.commit();
    }

    public String getKeyFilePath() {
        return sharedPreferences.getString(KEY_RECV_FILE_ABS_PATH, "");
    }

    public void setKeyFileType(FileType fileType) {
        editor.putString(KEY_RECV_FILE_TYPE, String.valueOf(fileType));
        editor.commit();
    }

    public String getKeyFileType() {
        return sharedPreferences.getString(KEY_RECV_FILE_TYPE, "");
    }


    public void setKeyJson(String json) {
        editor.putString(KEY_RECV_DB_JSON, json);
        editor.commit();
    }

    public String getKeyJson() {
        return sharedPreferences.getString(KEY_RECV_DB_JSON, "");
    }

}
