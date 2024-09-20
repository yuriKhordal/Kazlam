package com.yurikh.kazlam.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity (tableName = "Units", foreignKeys = {
   @ForeignKey(entity = Unit.class, parentColumns = "id",
      childColumns = "motherUnitId", onDelete = ForeignKey.RESTRICT),
   @ForeignKey(entity = Soldier.class, parentColumns = "id",
      childColumns = "commanderId", onDelete = ForeignKey.RESTRICT)
})
public class Unit {
   @PrimaryKey(autoGenerate = true)
   public long id;
   @Nullable
   @ColumnInfo(index = true)
   public Long motherUnitId;
   @Nullable
   @ColumnInfo(index = true)
   public Long commanderId;
   @NonNull
   public String name;

   public Unit(long id, @Nullable Long motherUnitId,
   @Nullable Long commanderId, @NonNull String name) {
      this.id = id;
      this.motherUnitId = motherUnitId;
      this.commanderId = commanderId;
      this.name = name;
   }

   @Ignore
   public Unit(@Nullable Long motherUnitId,
   @Nullable Long commanderId, @NonNull String name) {
      this.motherUnitId = motherUnitId;
      this.commanderId = commanderId;
      this.name = name;
   }

   @Ignore
   public Unit(@Nullable Long motherUnitId, @NonNull String name) {
      this.motherUnitId = motherUnitId;
      this.name = name;
   }

   @Ignore
   public Unit(@NonNull String name) {
      this.name = name;
   }

   @NonNull
   @Override
   public String toString() {
      return name;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Unit)) return false;
      Unit unit = (Unit)o;
      return id == unit.id
         && Objects.equals(motherUnitId, unit.motherUnitId)
         && Objects.equals(commanderId, unit.commanderId)
         && Objects.equals(name, unit.name);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(id);
   }
}
