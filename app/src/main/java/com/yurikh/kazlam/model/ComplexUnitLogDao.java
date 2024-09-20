package com.yurikh.kazlam.model;

import androidx.room.Dao;
import androidx.room.Query;

import com.yurikh.kazlam.KazlamApp;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;

@Dao
public abstract class ComplexUnitLogDao implements UnitLogDao {

   public Completable delete(UnitLog log) {
      return delete(log.id);
   }

   public Completable delete(long logId) {
      return Completable.fromAction(() -> {
         UnitLogSoldierDao dao = KazlamApp.getDatabase().unitLogSoldiersDao();

//         List<UnitLogSoldier> logSoldiers = dao.getByLog(logId).blockingGet();
//         List<Completable> deleteList = logSoldiers.stream()
//            .map(dao::delete).collect(Collectors.toList());
//
//         Completable.merge(deleteList).blockingAwait();
         dao.deleteByLog(logId).blockingAwait();
         deleteInternal(logId).blockingAwait();
      });
   }

   @Query("DELETE FROM UnitLogs WHERE id=:id")
   protected abstract Completable deleteInternal(long id);

   public Completable deleteByUnit(long unitId) {
      return deleteSoldierLogsByUnit(unitId)
         .andThen(deleteByUnitInternal(unitId));
   }

   @Query("DELETE FROM UnitLogSoldiers WHERE logId IN (" +
      "SELECT id FROM UnitLogs WHERE unitId=:unitId)")
   protected abstract Completable deleteSoldierLogsByUnit(long unitId);

   @Query("DELETE FROM UnitLogs WHERE unitId=:unitId")
   protected abstract Completable deleteByUnitInternal(long unitId);

}
