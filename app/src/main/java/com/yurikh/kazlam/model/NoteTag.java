package com.yurikh.kazlam.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "NoteTags", indices = @Index(value = "name"))
public class NoteTag {
   @PrimaryKey(autoGenerate = true)
   public long id;
   @NonNull
   public String name;

   public NoteTag(long id, @NonNull String name) {
      this.id = id;
      this.name = name;
   }

   @Ignore
   public NoteTag(@NonNull String name) {
      this.name = name;
   }

   @NonNull
   @Override
   public String toString() {
      return name;
   }
}
