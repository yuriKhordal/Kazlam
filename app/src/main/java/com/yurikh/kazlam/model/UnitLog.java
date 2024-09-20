package com.yurikh.kazlam.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "UnitLogs", foreignKeys = @ForeignKey(
   entity = Unit.class, parentColumns = "id", childColumns = "unitId",
   onDelete = ForeignKey.RESTRICT))
public class UnitLog {
   @PrimaryKey(autoGenerate = true)
   public long id;
   @ColumnInfo(index = true)
   public long unitId;
   @NonNull
   public String title;
   @NonNull
   public String date;

   public UnitLog(long id, long unitId, @NonNull String title, @NonNull String date) {
      this.id = id;
      this.unitId = unitId;
      this.title = title;
      this.date = date;
   }

   @Ignore
   public UnitLog(long unitId, @NonNull String title, @NonNull String date) {
      this.unitId = unitId;
      this.title = title;
      this.date = date;
   }
}
