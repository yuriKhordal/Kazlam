package com.yurikh.kazlam.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "LeaveRequests", foreignKeys = @ForeignKey(
   entity = Soldier.class, parentColumns = "id",
   childColumns = "soldierId", onDelete = ForeignKey.RESTRICT
))
public class LeaveRequest {
   @PrimaryKey(autoGenerate = true)
   public long id;
   @ColumnInfo(index = true)
   public long soldierId;
   @NonNull
   public String date;
   @NonNull
   public String returnDate;
   @NonNull
   public String reason;
   @NonNull
   public String status;

   public LeaveRequest(long id, long soldierId, @NonNull String date,
                       @NonNull String returnDate, @NonNull String reason,
                       @NonNull String status) {
      this.id = id;
      this.soldierId = soldierId;
      this.date = date;
      this.returnDate = returnDate;
      this.reason = reason;
      this.status = status;
   }

   @Ignore
   public LeaveRequest(long soldierId, @NonNull String date, @NonNull String returnDate,
                       @NonNull String reason, @NonNull String status) {
      this.soldierId = soldierId;
      this.date = date;
      this.returnDate = returnDate;
      this.reason = reason;
      this.status = status;
   }
}
