package com.example.testapp.RoomDb.Repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.testapp.RoomDb.Data;
import com.example.testapp.RoomDb.Database.SampleDatabase;
import com.example.testapp.RoomDb.Interface.DataDao;

import java.util.List;

public class DataRepository {
    private DataDao dataDao;
    private LiveData<List<Data>> allDatas;

    public DataRepository(Application application) {
        SampleDatabase database = SampleDatabase.getInstance(application);
        dataDao = database.dataDao();
        allDatas = dataDao.getAllDatas();
    }

    public void insert(Data data){
        new InsertDataAsyncTask(dataDao).execute(data);
    }

    public void update(Data data){
        new UpdateDataAsyncTask(dataDao).execute(data);
    }

    public void delete(Data data){
        new DeleteDataAsyncTask(dataDao).execute(data);
    }

    public void deleteAllDatas(){
        new DeleteAllDatasAsyncTask(dataDao).execute();
    }

    public LiveData<List<Data>> getAllDatas() {
        return allDatas;
    }

    private static class InsertDataAsyncTask extends AsyncTask<Data, Void, Void> {
        private DataDao dataDao;

        InsertDataAsyncTask(DataDao dataDao) {
            this.dataDao = dataDao;
        }

        @Override
        protected Void doInBackground(Data... datas) {
            dataDao.insert(datas[0]);
            return null;
        }
    }

    private static class UpdateDataAsyncTask extends AsyncTask<Data, Void, Void> {
        private DataDao dataDao;

        UpdateDataAsyncTask(DataDao dataDao) {
            this.dataDao = dataDao;
        }

        @Override
        protected Void doInBackground(Data... datas) {
            dataDao.update(datas[0]);
            return null;
        }
    }

    private static class DeleteDataAsyncTask extends AsyncTask<Data, Void, Void> {
        private DataDao dataDao;

        DeleteDataAsyncTask(DataDao dataDao) {
            this.dataDao = dataDao;
        }

        @Override
        protected Void doInBackground(Data... datas) {
            dataDao.delete(datas[0]);
            return null;
        }
    }

    private static class DeleteAllDatasAsyncTask extends AsyncTask<Void, Void, Void> {
        private DataDao dataDao;

        DeleteAllDatasAsyncTask(DataDao dataDao) {
            this.dataDao = dataDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dataDao.deleteAllDatas();
            return null;
        }
    }

    private static class GetAllDatasAsyncTask extends AsyncTask<Void, Void, Void> {
        private DataDao dataDao;

        GetAllDatasAsyncTask(DataDao dataDao) {
            this.dataDao = dataDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dataDao.getAllDatas();
            return null;
        }
    }
}
