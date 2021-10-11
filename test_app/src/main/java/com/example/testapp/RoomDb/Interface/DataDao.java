package com.example.testapp.RoomDb.Interface;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import androidx.lifecycle.LiveData;


import com.example.testapp.RoomDb.Data;

import java.util.List;

@Dao
public interface DataDao {
    @Insert
    void insert(Data data);

    @Update
    void update(Data data);

    @Delete
    void delete(Data data);

    @Query("DELETE FROM sample_table")
    void deleteAllDatas();

    @Query("SELECT * from sample_table ORDER BY id ASC")
    LiveData<List<Data>> getAllDatas();
}
