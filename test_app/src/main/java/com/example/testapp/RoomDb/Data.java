package com.example.testapp.RoomDb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "sample_table")
public class Data {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String data;

    public Data(String data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
