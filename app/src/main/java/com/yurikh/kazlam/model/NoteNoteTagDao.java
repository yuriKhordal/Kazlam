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
public interface NoteNoteTagDao {

   @Query("SELECT * FROM NotesNoteTags")
   Single<List<NoteNoteTag>> getAll();

   @Query("SELECT * FROM NotesNoteTags ORDER BY noteId")
   Single<List<NoteNoteTag>> getAllSortedByNote();

   @Query("SELECT * FROM NotesNoteTags WHERE id=:id")
   Maybe<NoteNoteTag> getById(long id);

   @Query("SELECT * FROM NotesNoteTags WHERE noteId=:noteId")
   Single<List<NoteNoteTag>> getByNote(long noteId);

   @Query("SELECT * FROM NotesNoteTags WHERE noteId IN(:noteIds) ORDER BY noteId")
   Single<List<NoteNoteTag>> getByNotesSortedByNote(List<Long> noteIds);

   @Query("SELECT * FROM NotesNoteTags WHERE noteTagId=:tagId")
   Single<List<NoteNoteTag>> getByTag(long tagId);

   @Query("INSERT INTO NotesNoteTags(noteId, noteTagId) VALUES" +
      "(:noteId, (SELECT id FROM NoteTags WHERE name=:tagName))")
   Completable insertByName(long noteId, String tagName);

   @Insert
   Single<Long> insert(NoteNoteTag note);

   @Update
   Completable update(NoteNoteTag note);

   @Delete
   Completable delete(NoteNoteTag note);

   @Query("DELETE FROM NotesNoteTags WHERE noteId=:noteId")
   Completable deleteByNote(long noteId);

   @Query("DELETE FROM NotesNoteTags WHERE noteTagId=:tagId")
   Completable deleteByTag(long tagId);

   @Query("DELETE FROM NotesNoteTags WHERE noteId=:noteId AND noteTagId=(SELECT id FROM NoteTags WHERE name=:tagName)")
   Completable deleteByName(long noteId, String tagName);

   @Query("DELETE FROM NotesNoteTags WHERE noteId=:noteId AND noteTagId IN" +
      "(SELECT id FROM NoteTags WHERE name IN(:tagNames))")
   Completable deleteByNames(long noteId, List<String> tagNames);
}
