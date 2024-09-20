package com.yurikh.kazlam.viewmodel;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.KazlamDb;
import com.yurikh.kazlam.UnitTree;
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.model.UnitLog;
import com.yurikh.kazlam.model.UnitLogSoldier;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UnitLogViewModel {
   // The names are the names of the columns, the values are the indicies
   // in the `unit_logs_sort_by` array
   public enum SortBy {
      title(0), unitId(1), date(2);
      public final int val;
      SortBy(int val) { this.val = val; }
      public static SortBy fromInt(int val) {
         switch (val) {
            case 0: return title;
            case 1: return unitId;
            case 2: return date;
            default: throw new IllegalArgumentException("Can't convert (" + val + ") to " + SortBy.class);
         }
      }
   }

   public static class UnitLogSoldierWrapper {
      public UnitLogSoldier entry;
      public UnitLog log;
      public Soldier soldier;

      public UnitLogSoldierWrapper(UnitLogSoldier entry, UnitLog log, Soldier soldier) {
         this.entry = entry;
         this.log = log;
         this.soldier = soldier;
      }
   }

   public static class UnitLogWrapper {
      public UnitLog log;
      public Unit unit;
      public List<UnitLogSoldierWrapper> soldiers;

      public UnitLogWrapper(UnitLog log, Unit unit) {
         this.log = log;
         this.unit = unit;
         soldiers = null;
      }

      public UnitLogWrapper(UnitLog log, Unit unit, List<UnitLogSoldierWrapper> soldiers) {
         this.log = log;
         this.unit = unit;
         this.soldiers = soldiers;
      }
   }

   Context ctx;

   public UnitLogViewModel(Context ctx) {
      this.ctx = ctx;
   }

   public Single<UnitLogWrapper> getLog(long id) {
      return Single.fromCallable(() -> {
         KazlamDb db = KazlamApp.getDatabase();
         UnitLog log = db.unitLogsDao().getById(id).blockingGet();
         Unit unit = db.unitsDao().getById(log.unitId).blockingGet();

         // Map soldiers by id:
         List<Soldier> soldiers = db.soldiersDao().getAll().blockingGet();
         Map<Long, Soldier> soldierMap = new HashMap<>();
         soldiers.forEach(soldier -> soldierMap.put(soldier.id, soldier));

         List<UnitLogSoldier> logSoldiers = db.unitLogSoldiersDao()
            .getByLog(id).blockingGet();
         List<UnitLogSoldierWrapper> logSoldierWrappers = logSoldiers.stream()
            .map(sold -> new UnitLogSoldierWrapper(sold, log, soldierMap.get(sold.soldierId)))
            .collect(Collectors.toList());

         return new UnitLogWrapper(log, unit, logSoldierWrappers);
      }).subscribeOn(Schedulers.io());
   }

   public Single<Unit> getUnit(long unitId) {
      KazlamDb db = KazlamApp.getDatabase();
      return Single.fromCallable(db.unitsDao().getById(unitId)::blockingGet)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread());
   }

   public Single<List<UnitLogWrapper>> loadLogs(int sortBy, boolean asc) {
      return Single.fromCallable(() -> {
         KazlamDb db = KazlamApp.getDatabase();
         List<Unit> units = db.unitsDao().getAll().blockingGet();
         List<UnitLog> logs = db.unitLogsDao().getAll().blockingGet();

         Map<Long, Unit> unitMap = new HashMap<>();
         units.forEach(unit -> unitMap.put(unit.id, unit));

         List<UnitLogWrapper> wrappers = logs.stream()
            .map(log -> new UnitLogWrapper(log, unitMap.get(log.unitId)))
            .collect(Collectors.toList());

         sortBy(wrappers, sortBy, asc);

         return wrappers;
      }).subscribeOn(Schedulers.io());
   }

   public Single<List<UnitLogWrapper>> loadLogsByUnit(long unitId, int sortBy, boolean asc) {
      return Single.fromCallable(() -> {
         KazlamDb db = KazlamApp.getDatabase();
         UnitTree tree = new UnitTree();
         tree.fill(unitId);

         List<Unit> units = tree.flattenUnits();
         List<Long> unitIds = units.stream().map(unit -> unit.id)
            .collect(Collectors.toList());
         List<UnitLog> logs = db.unitLogsDao().getByUnits(unitIds).blockingGet();

         Map<Long, Unit> unitMap = new HashMap<>();
         units.forEach(unit -> unitMap.put(unit.id, unit));

         List<UnitLogWrapper> wrappers = logs.stream()
            .map(log -> new UnitLogWrapper(log, unitMap.get(log.unitId)))
            .collect(Collectors.toList());

         sortBy(wrappers, sortBy, asc);

         return wrappers;
      }).subscribeOn(Schedulers.io());
   }

   public Completable fillUnitAdapter(ArrayAdapter<Unit> adapter) {
      return Completable.fromAction(() -> {
         KazlamDb db = KazlamApp.getDatabase();
         List<Unit> units = db.unitsDao().getAll().blockingGet();
         adapter.addAll(units);
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public void sortBy(List<UnitLogWrapper> list, int by, boolean asc) {
      Comparator<UnitLogWrapper> comp;
      if (by == SortBy.title.val) {
         comp = Comparator.comparing(item -> item.log.title);
      } else if (by == SortBy.unitId.val) {
         comp = Comparator.comparing(item -> item.unit.name);
      } else if (by == SortBy.date.val) {
         comp = Comparator.comparing(item -> item.log.date);
      } else return;

      if (!asc) comp = comp.reversed();
      list.sort(comp);
   }

   public Single<Long> addLog(long unitId, String title, String date) {
      return Single.fromCallable(() -> {
         KazlamDb db = KazlamApp.getDatabase();
         UnitLog log = new UnitLog(unitId, title, date);
         log.id = db.unitLogsDao().insert(log).blockingGet();

         UnitTree tree = new UnitTree();
         tree.fill(unitId);
         tree.fillSoldiers();

         List<UnitLogSoldier> soldiers = tree.flattenSoldiers().stream()
            .map(soldier -> new UnitLogSoldier(log.id, soldier.id))
            .collect(Collectors.toList());

         db.unitLogSoldiersDao().insert(soldiers).blockingSubscribe();

         return log.id;
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Completable updateLog(UnitLogWrapper item, String title,
                                String date, List<String> values) {
      return Completable.fromAction(() -> {
         KazlamDb db = KazlamApp.getDatabase();

         item.log.title = title;
         item.log.date = date;
         for (int i = 0; i < item.soldiers.size(); i++) {
            item.soldiers.get(i).entry.value = values.get(i);
         }

         List<UnitLogSoldier> soldiers = item.soldiers.stream()
            .map(wrap -> wrap.entry)
            .collect(Collectors.toList());

         db.unitLogSoldiersDao().update(soldiers).blockingAwait();
         db.unitLogsDao().update(item.log);
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Completable deleteLog(long id) {
      return KazlamApp.getDatabase().unitLogsDao().delete(id)
         .subscribeOn(Schedulers.io());
   }

}
