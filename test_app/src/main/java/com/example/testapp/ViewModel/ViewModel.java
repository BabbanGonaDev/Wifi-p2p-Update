package com.example.testapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.testapp.RoomDb.Data;
import com.example.testapp.RoomDb.Repository.DataRepository;

import java.util.List;

public class ViewModel extends AndroidViewModel {
    private DataRepository dataRepository;
    private List<Data> allDatas;

    public ViewModel(@NonNull Application application) {
        super(application);
        dataRepository = new DataRepository(application);
        allDatas = dataRepository.getAllDatas();

    }

    // Methods to insert, update and delete Datas
    public void insertData(Data data){
        dataRepository.insert(data);
    }
    public void updateData(Data data){
        dataRepository.update(data);
    }
    public void deleteData(Data data){
        dataRepository.delete(data);
    }
    public void deleteAllDatas(){
        dataRepository.deleteAllDatas();
    }
    public List<Data> getAllDatas(){
        return allDatas;
    }
}
