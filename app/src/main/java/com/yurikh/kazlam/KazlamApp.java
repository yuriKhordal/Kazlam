package com.yurikh.kazlam;

import android.app.Application;

import androidx.room.Room;

public class KazlamApp extends Application {
   static volatile KazlamDb database;

   public static KazlamDb getDatabase() { return database; }

   @Override
   public void onCreate() {
      super.onCreate();
      database = Room.databaseBuilder(getApplicationContext(),
         KazlamDb.class, KazlamDb.DATABASE_NAME).build();

      // DEBUG
//      database.insertDebugData();
   }
}
