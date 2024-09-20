package com.yurikh.kazlam.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface NoteDao {
   @Query("SELECT * FROM Notes ORDER BY modifyDate")
   Single<List<Note>> getAll();

   @Query("SELECT * FROM Notes ORDER BY id")
   Single<List<Note>> getAllSortedId();

   @Query("SELECT * FROM Notes WHERE id=:id")
   Maybe<Note> getById(long id);

   @Query("SELECT * FROM Notes WHERE soldierId=:soldierId ORDER BY modifyDate")
   Single<List<Note>> getBySoldier(long soldierId);

   @Query("SELECT * FROM Notes WHERE soldierId=:soldierId ORDER BY id")
   Single<List<Note>> getBySoldierSortedById(long soldierId);

   @Query("SELECT * FROM Notes WHERE soldierId IN(:soldierIds) ORDER BY modifyDate")
   Single<List<Note>> getBySoldiers(List<Long> soldierIds);

   @Query("SELECT * FROM Notes WHERE soldierId IN(:soldierIds) ORDER BY id")
   Single<List<Note>> getBySoldiersSortedById(List<Long> soldierIds);

   @Query("SELECT * FROM Notes WHERE id IN (SELECT noteId FROM NotesNoteTags WHERE noteTagId=:tagId) ORDER BY modifyDate")
   Single<List<Note>> getByTag(long tagId);

   @Insert
   Single<Long> insert(Note note);

   @Update
   Completable update(Note note);
}
