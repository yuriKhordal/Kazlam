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

         List<UnitLogSoldier> logSoldiers = dao.getByLog(logId).blockingGet();
         List<Completable> deleteList = logSoldiers.stream()
            .map(dao::delete).collect(Collectors.toList());

         Completable.merge(deleteList).blockingAwait();
         deleteInternal(logId).blockingAwait();
      });
   }

   @Query("DELETE FROM unitlogs WHERE id=:id")
   protected abstract Completable deleteInternal(long id);
}
