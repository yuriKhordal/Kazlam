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
public interface SoldierDao {

   @Query("SELECT * FROM Soldiers")
   Single<List<Soldier>> getAll();

   @Query("SELECT * FROM Soldiers ORDER BY :sortBy")
   Single<List<Soldier>> getAll(String sortBy);

   @Query("SELECT DISTINCT role FROM Soldiers")
   Single<List<String>> getRoles();

   @Query("SELECT DISTINCT rank FROM Soldiers")
   Single<List<String>> getRanks();

   @Query("SELECT * FROM Soldiers WHERE id=:id")
   Maybe<Soldier> getById(long id);

   /**
    * Get a list of all the soldiers in a unit(but not sub units).
    * @param unitId The id of the unit to query.
    * @Note To get all soldiers of the unit and subunits recursively use
    * {@link com.yurikh.kazlam.UnitTree}.
    */
   @Query("SELECT * FROM Soldiers WHERE unitId=:unitId")
   Single<List<Soldier>> getByUnit(long unitId);

   @Insert
   Single<Long> insert(Soldier soldier);

   @Update
   Completable update(Soldier soldier);
}
