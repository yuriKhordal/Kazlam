package com.yurikh.kazlam.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "UnitLogSoldiers", foreignKeys = {
   @ForeignKey(entity = UnitLog.class, parentColumns = "id",
      childColumns = "logId", onDelete = ForeignKey.RESTRICT),
   @ForeignKey(entity = Soldier.class, parentColumns = "id",
      childColumns = "soldierId", onDelete = ForeignKey.RESTRICT)
})
public class UnitLogSoldier {
   @PrimaryKey(autoGenerate = true)
   public long id;
   @ColumnInfo(index = true)
   public long logId;
   @ColumnInfo(index = true)
   public long soldierId;
   @NonNull
   public String value;

   public UnitLogSoldier(long id, long logId, long soldierId, @NonNull String value) {
      this.id = id;
      this.logId = logId;
      this.soldierId = soldierId;
      this.value = value;
   }

   @Ignore
   public UnitLogSoldier(long logId, long soldierId, @NonNull String value) {
      this.logId = logId;
      this.soldierId = soldierId;
      this.value = value;
   }

   @Ignore
   public UnitLogSoldier(long logId, long soldierId) {
      this.logId = logId;
      this.soldierId = soldierId;
      this.value = "";
   }
}
