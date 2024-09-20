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
public interface DisciplinaryNoticeDao {

   @Query("SELECT * FROM Disciplinary")
   Single<List<DisciplinaryNotice>> getAll();

   @Query("SELECT * FROM Disciplinary WHERE id=:id")
   Maybe<DisciplinaryNotice> getById(long id);

   @Query("SELECT * FROM Disciplinary WHERE soldierId=:soldierId")
   Single<List<DisciplinaryNotice>> getBySoldier(long soldierId);

   @Query("SELECT * FROM Disciplinary WHERE soldierId IN(:soldierIds)")
   Single<List<DisciplinaryNotice>> getBySoldiers(List<Long> soldierIds);

   @Insert
   Single<Long> insert(DisciplinaryNotice note);

   @Update
   Completable update(DisciplinaryNotice note);

   @Delete
   Completable delete(DisciplinaryNotice note);

   @Query("DELETE FROM Disciplinary WHERE soldierId=:soldierId")
   Completable deleteBySoldier(long soldierId);
}
