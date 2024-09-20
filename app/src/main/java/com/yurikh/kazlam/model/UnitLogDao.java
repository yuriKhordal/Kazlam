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
public interface UnitLogDao {
   @Query("SELECT * FROM UnitLogs")
   Single<List<UnitLog>> getAll();

   @Query("SELECT * FROM UnitLogs WHERE id=:id")
   Maybe<UnitLog> getById(long id);

   @Query("SELECT * FROM UnitLogs WHERE unitId IN(:unitIds)")
   Single<List<UnitLog>> getByUnits(List<Long> unitIds);

   @Insert
   Single<Long> insert(UnitLog log);

   @Update
   Completable update(UnitLog log);

}
