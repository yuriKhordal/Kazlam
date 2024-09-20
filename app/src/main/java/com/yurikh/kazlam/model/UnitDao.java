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
public interface UnitDao {
   @Query("SELECT * FROM Units")
   Single<List<Unit>> getAll();

   @Query("SELECT * FROM Units WHERE id=:id")
   Maybe<Unit> getById(long id);

   /**
    * Get the direct (level 1) children of a unit with the specified id.
    * Does NOT return grandchildren or lower, only direct children.
    * @param id The id of the children's mother unit.
    */
   @Query("SELECT * FROM Units WHERE motherUnitId=:id")
   Single<List<Unit>> getDirectChildren(long id);

   @Query("SELECT * FROM Units WHERE id=(SELECT unitId FROM Soldiers WHERE id=:soldierId)")
   Single<Unit> getBySoldier(long soldierId);

   @Query("SELECT * FROM Units WHERE id IN(SELECT unitId FROM Soldiers WHERE id IN (:soldierIds))")
   Single<List<Unit>> getBySoldiers(List<Long> soldierIds);

   /**
    * Get a list of the top level (motherless) units, who may have daughter
    * units but do not have a mother unit. The matriarchs.
    */
   @Query("SELECT * FROM Units WHERE motherUnitId IS NULL")
   Single<List<Unit>> getTopLevelUnits();

   @Insert
   Single<Long> insert(Unit unit);

   @Update
   Completable update(Unit unit);
}
