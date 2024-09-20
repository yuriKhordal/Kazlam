package com.yurikh.kazlam.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "NotesNoteTags", foreignKeys = {
   @ForeignKey(entity = Note.class, parentColumns = "id",
      childColumns = "noteId", onDelete = ForeignKey.RESTRICT),
   @ForeignKey(entity = NoteTag.class, parentColumns = "id",
      childColumns = "noteTagId", onDelete = ForeignKey.RESTRICT)
})
public class NoteNoteTag {
   @PrimaryKey(autoGenerate = true)
   public long id;
   @ColumnInfo(index = true)
   public long noteId;
   @ColumnInfo(index = true)
   public long noteTagId;

   public NoteNoteTag(long id, long noteId, long noteTagId) {
      this.id = id;
      this.noteId = noteId;
      this.noteTagId = noteTagId;
   }

   @Ignore
   public NoteNoteTag(long noteId, long noteTagId) {
      this.noteId = noteId;
      this.noteTagId = noteTagId;
   }
}
