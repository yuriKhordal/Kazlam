package com.yurikh.kazlam;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.yurikh.kazlam.model.*;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Database(entities = {
   DisciplinaryNotice.class, LeaveRequest.class, Note.class,
   NoteNoteTag.class, NoteTag.class, Soldier.class, Unit.class,
   UnitLog.class, UnitLogSoldier.class
}, version = 1)
public abstract class KazlamDb extends RoomDatabase {
   /**The name of the local database file.*/
   public static final String DATABASE_NAME = "kazlam.db";

   public abstract DisciplinaryNoticeDao disciplinaryDao();

   public abstract ComplexNoteDao notesDao();

   public abstract NoteNoteTagDao noteNoteTagsDao();

   public abstract ComplexNoteTagDao noteTagsDao();

   public abstract  ComplexSoldierDao soldiersDao();

   public abstract ComplexUnitDao unitsDao();

   public abstract LeaveRequestDao leaveRequestDao();

   public abstract ComplexUnitLogDao unitLogsDao();

   public abstract UnitLogSoldierDao unitLogSoldiersDao();

   // ========================= DEBUG STUFF =========================

//   public void insertDebugData() {
//      DebugDao dao = debugDao();
//      Completable.fromAction(() -> {
//         clearDB();
//         dao.debugUnits();
//         dao.debugSoldiers();
//         dao.setCommanders();
//         dao.debugNotes();
//         dao.debugNoteTags();
//         dao.debugNoteNoteTags();
//         dao.debugDisciplinaryNotices();
//         dao.debugLeaveRequests();
//         dao.debugUnitLogs();
//         dao.debugUnitLogSoldiers();
//      }).subscribeOn(Schedulers.io()).blockingAwait();
//   }
//
//   private void clearDB() {
//      DebugDao dao = debugDao();
//      dao.deleteLeaveRequests();
//      dao.deleteDisciplinaryNotices();
//      dao.deleteUnitLogs();
//      dao.deleteNotes();
//      dao.unsetCommanders();
//      dao.deleteSoldiers();
//
//      List<Unit> units = unitsDao().getTopLevelUnits().blockingGet();
//      Completable.merge(units.stream().map(unit -> unitsDao().delete(unit))
//         .collect(Collectors.toList())).blockingAwait();
//   }
//
//   protected abstract DebugDao debugDao();

}
