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
public interface UnitLogSoldierDao {
   @Query("SELECT * FROM UnitLogSoldiers")
   Single<List<UnitLogSoldier>> getAll();

   @Query("SELECT * FROM UnitLogSoldiers WHERE id=:id")
   Maybe<UnitLogSoldier> getById(long id);

   @Query("SELECT * FROM UnitLogSoldiers WHERE logId=:logId")
   Single<List<UnitLogSoldier>> getByLog(long logId);

   @Insert
   Single<Long> insert(UnitLogSoldier logSoldier);

   @Insert
   Single<List<Long>> insert(List<UnitLogSoldier> logSoldier);

   @Update
   Completable update(UnitLogSoldier logSoldier);

   @Update
   Completable update(List<UnitLogSoldier> logSoldier);

   @Delete
   Completable delete(UnitLogSoldier logSoldier);

   @Query("DELETE FROM UnitLogSoldiers WHERE soldierId=:soldierId")
   Completable deleteBySoldier(long soldierId);
}
