package com.yurikh.kazlam.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface NoteTagDao {
   @Query("SELECT * FROM NoteTags")
   Single<List<NoteTag>> getAll();

   @Query("SELECT * FROM NoteTags ORDER BY id")
   Single<List<NoteTag>> getAllSortedById();

   @Query("SELECT * FROM NoteTags WHERE id=:id")
   Maybe<NoteTag> getById(long id);

   /**
    * Get all the tags that a note is tagged with.
    * @param noteId The id of the note.
    */
   @Query("SELECT * FROM NoteTags WHERE id IN(SELECT noteTagId FROM NotesNoteTags WHERE noteId=:noteId)")
   Single<List<NoteTag>> getByNote(long noteId);

   @Insert
   Single<Long> insert(NoteTag note);

   @Update
   Completable update(NoteTag note);

   @Query("DELETE FROM NoteTags WHERE id NOT IN (" +
      "SELECT DISTINCT noteTagId FROM NotesNoteTags" +
      ")")
   Completable deleteUnused();

   @Delete
   Completable delete(NoteTag note);
}
