package com.yurikh.kazlam.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Disciplinary", foreignKeys = @ForeignKey(
   entity = Soldier.class, parentColumns = "id",
   childColumns = "soldierId", onDelete = ForeignKey.RESTRICT
))
public class DisciplinaryNotice {
   @PrimaryKey (autoGenerate = true)
   public long id;
   @ColumnInfo(index = true)
   public long soldierId;
   @NonNull
   public String date;
   @NonNull
   public String title;
   @NonNull
   public String description;
   @NonNull
   public String punishment;

   public DisciplinaryNotice(long id, long soldierId, @NonNull String date,
   @NonNull String title, @NonNull String description,
   @NonNull String punishment) {
      this.id = id;
      this.soldierId = soldierId;
      this.date = date;
      this.title = title;
      this.description = description;
      this.punishment = punishment;
   }

   @Ignore
   public DisciplinaryNotice(long soldierId, @NonNull String date,
   @NonNull String title, @NonNull String description,
   @NonNull String punishment) {
      this.soldierId = soldierId;
      this.date = date;
      this.title = title;
      this.description = description;
      this.punishment = punishment;
   }
}
