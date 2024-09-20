package com.yurikh.kazlam.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.yurikh.kazlam.Helper;

import java.util.Date;

@Entity(tableName = "Notes", foreignKeys = @ForeignKey(
   entity = Soldier.class, parentColumns = "id",
   childColumns = "soldierId", onDelete = ForeignKey.RESTRICT
))
public class Note {
   @PrimaryKey(autoGenerate = true)
   public long id;
   @ColumnInfo(index = true)
   public long soldierId;
   public String title;
   public String content;
   @NonNull
   public String createDate;
   @NonNull
   public String modifyDate;

   public Note(long id, long soldierId, String title, String content,
      @NonNull String createDate, @NonNull String modifyDate) {
      this.id = id;
      this.soldierId = soldierId;
      this.title = title;
      this.content = content;
      this.createDate = createDate;
      this.modifyDate = modifyDate;
   }

   @Ignore
   public Note(long soldierId, String title, String content) {
      String now = Helper.DATETIME_FORMATTER.format(new Date());
      this.soldierId = soldierId;
      this.title = title;
      this.content = content;
      this.createDate = this.modifyDate = now;
   }
}
