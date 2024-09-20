package com.yurikh.kazlam.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(tableName = "Soldiers",
   foreignKeys = @ForeignKey(entity = Unit.class, parentColumns = "id",
      childColumns = "unitId", onDelete = ForeignKey.RESTRICT),
   indices = @Index(value = "mpId")
)
public class Soldier {
   @PrimaryKey
   public long id;
   public int mpId; // Military Personal ID. (Mispar Ishi)
   @ColumnInfo(index = true)
   public long unitId;
   @NonNull
   public String name;
   @NonNull
   public String rank;
   @NonNull
   public String role;

   public Soldier(long id, int mpId, long unitId, @NonNull String name, @NonNull String rank, @NonNull String role) {
      this.id = id;
      this.unitId = unitId;
      this.mpId = mpId;
      this.name = name;
      this.rank = rank;
      this.role = role;
   }

   @Ignore
   public Soldier(int mpId, long unitId, @NonNull String name,
                  @NonNull String rank, @NonNull String role) {
      this.mpId = mpId;
      this.unitId = unitId;
      this.name = name;
      this.rank = rank;
      this.role = role;
   }

   @Ignore
   public Soldier(int mpId, long unitId, @NonNull String name) {
      this(mpId, unitId, name, "", "");
   }

   @NonNull
   @Override
   public String toString() {
      return name;
   }

}
