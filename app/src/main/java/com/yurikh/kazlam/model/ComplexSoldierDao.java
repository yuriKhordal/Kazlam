package com.yurikh.kazlam.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;

import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.KazlamDb;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;

@Dao
public abstract class ComplexSoldierDao implements SoldierDao {
   public Completable delete(Soldier soldier) {
      KazlamDb db = KazlamApp.getDatabase();

      Completable delLeaveRequests = db.leaveRequestDao().deleteBySoldier(soldier.id);
      Completable delDiscipline = db.disciplinaryDao().deleteBySoldier(soldier.id);
      Completable delNotes = db.notesDao().deleteBySoldier(soldier.id);
      Completable delLogs = db.unitLogSoldiersDao().deleteBySoldier(soldier.id);
      Completable unsetCommand = unsetCommander(soldier.id);

      return Completable.mergeArray(unsetCommand, delNotes, delDiscipline, delLeaveRequests, delLogs)
         .andThen(deleteInternal(soldier));
   }

   public Completable delete(List<Soldier> soldiers) {
      return Completable.merge(soldiers.stream()
         .map(this::delete).collect(Collectors.toList()));
   }

   @Query("UPDATE Units SET commanderId=null WHERE commanderId=:id")
   protected abstract Completable unsetCommander(long id);

   @Query("UPDATE Units SET commanderId=null WHERE commanderId IN(:ids)")
   protected abstract Completable unsetCommanders(List<Long> ids);

   @Delete
   protected abstract Completable deleteInternal(Soldier soldier);
}
