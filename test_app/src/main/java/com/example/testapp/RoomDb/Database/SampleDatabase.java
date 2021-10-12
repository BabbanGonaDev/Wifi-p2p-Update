package com.example.testapp.RoomDb.Database;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.example.testapp.RoomDb.Data;
import com.example.testapp.RoomDb.Interface.DataDao;


@Database(entities = {Data.class}, version = 1)
public abstract class SampleDatabase extends RoomDatabase {
    private static SampleDatabase instance;

    public abstract DataDao dataDao();


    public static synchronized SampleDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    SampleDatabase.class, "sample_database")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onCreate (@NonNull SupportSQLiteDatabase db){
                    super.onCreate(db);
                    new PopulateDbAsyncTask(instance).execute();
                }
            };
    /**
     * Populate the database in the background.
     */
    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private DataDao dataDao;

        PopulateDbAsyncTask(SampleDatabase db) {
            dataDao = db.dataDao();;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Populate Data database
            dataDao.insert(new Data("Frank Lampard"));
            dataDao.insert(new Data("David Beckham"));
            dataDao.insert(new Data("Ikechukwu Ejiofor"));
            dataDao.insert(new Data("Rehoboth Iyasele"));
            dataDao.insert(new Data("Buraimoh Kikiope"));
            dataDao.insert(new Data("Bayo Adeniran"));
            return null;
        }
    }


}
