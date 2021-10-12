package com.example.testapp.SharedPrefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.wifi_p2p.Enum.FileType;

public class SharedPrefs {
    private static final String KEY_RECV_DB_JSON = "received_db_json";
    private static final String KEY_RECV_JSON_FILE = "received_db_json";
    public static final String SHARED_PREFS = "shared_prefs";
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefs(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

    }

    public void setKeyJson(String json) {
        editor.putString(KEY_RECV_DB_JSON, json);
        editor.commit();
    }

    public String getKeyJson() {
        return sharedPreferences.getString(KEY_RECV_DB_JSON, "");
    }


    public void setKeyJsonFilePath(String json) {
        editor.putString(KEY_RECV_JSON_FILE, json);
        editor.commit();
    }

    public String getKeyJsonFilePath() {
        return sharedPreferences.getString(KEY_RECV_JSON_FILE, "");
    }
}
