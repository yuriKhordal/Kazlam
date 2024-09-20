package com.yurikh.kazlam.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;

import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.UnitTree;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public abstract class ComplexNoteDao implements NoteDao {

   Single<List<Note>> getByUnit(long unitId) {
      return Single.fromCallable(() -> {
         UnitTree tree = new UnitTree();
         tree.fill(unitId);
         tree.fillSoldiers();
         List<Long> ids = tree.flattenSoldiers().stream().map(sold -> sold.id)
                 .collect(Collectors.toList());
         return getBySoldiers(ids).blockingGet();
      });
   }

   public Completable deleteBySoldier(long soldierId) {
      return Completable.fromAction(() -> {
         List<Note> notes = getBySoldier(soldierId).blockingGet();
         Completable.merge(notes.stream().map(this::delete)
            .collect(Collectors.toList())).blockingAwait();
      });
   }

   public Completable delete(Note note) {
      return delete(note.id);
   }

   public Completable delete(long id) {
      return KazlamApp.getDatabase().noteNoteTagsDao().deleteByNote(id)
         .andThen(deleteInternal(id))
         .andThen(KazlamApp.getDatabase().noteTagsDao().deleteUnused());
   }

   @Delete
   protected abstract Completable deleteInternal(Note note);

   @Query("DELETE FROM Notes WHERE id=:id")
   protected abstract Completable deleteInternal(long id);
}
