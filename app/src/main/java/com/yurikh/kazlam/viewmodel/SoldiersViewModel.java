package com.yurikh.kazlam.viewmodel;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.RecyclerAdapter;
import com.yurikh.kazlam.UnitTree;
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.model.Unit;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SoldiersViewModel {
   // The names are the names of the columns, the values are the indicies
   // in the `soldiers_sortby` array
   public enum SortBy {
      mpId(0), name(1), unitId(2), rank(3), role(4);
      public final int val;
      SortBy(int val) { this.val = val; }
      public static SortBy fromInt(int val) {
         switch (val) {
         case 0: return mpId;
         case 1: return unitId;
         case 2: return name;
         case 3: return rank;
         case 4: return role;
         default: throw new IllegalArgumentException("Can't convert (" + val + ") to " + SortBy.class);
         }
      }
   }

   Context ctx;

   public SoldiersViewModel(Context ctx) {
      this.ctx = ctx;
   }

   public Soldier getSoldier(long id) {
      return KazlamApp.getDatabase().soldiersDao().getById(id)
         .subscribeOn(Schedulers.io())
         .blockingGet();
   }

   public Unit getUnit(long id) {
      return KazlamApp.getDatabase().unitsDao().getById(id)
              .subscribeOn(Schedulers.io())
              .blockingGet();
   }

   public List<Soldier> loadSoldiers(Map<Long, Unit> unitMap, int sortBy, boolean asc) {
      List<Unit> units = KazlamApp.getDatabase().unitsDao()
              .getAll().blockingGet();
      List<Soldier> soldiers = KazlamApp.getDatabase().soldiersDao()
              .getAll().blockingGet();

      sortBy(soldiers, unitMap, sortBy, asc);
      units.forEach(unit -> unitMap.put(unit.id, unit));


      return soldiers;
   }

   public List<Soldier> loadUnitSoldiers(long unitId, Map<Long, Unit> unitMap, int sortBy, boolean asc) {
      UnitTree tree = new UnitTree();
      tree.fill(unitId);
      tree.fillSoldiers();

      List<Unit> units = tree.flattenUnits();
      List<Soldier> soldiers = tree.flattenSoldiers();

      sortBy(soldiers, unitMap, sortBy, asc);
      units.forEach(unit -> unitMap.put(unit.id, unit));

      return soldiers;
   }

   public Completable fillRolesAdapter(ArrayAdapter<String> adapter) {
      return Completable.fromAction(() -> {
         adapter.addAll(KazlamApp.getDatabase().soldiersDao().getRoles()
            .subscribeOn(Schedulers.io()).blockingGet());
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Completable fillRanksAdapter(ArrayAdapter<String> adapter) {
      return Completable.fromAction(() -> {
         adapter.addAll(KazlamApp.getDatabase().soldiersDao().getRanks()
            .subscribeOn(Schedulers.io()).blockingGet());
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Completable fillUnitsAdapter(ArrayAdapter<Unit> adapter) {
      return Completable.fromAction(() -> {
         adapter.addAll(KazlamApp.getDatabase().unitsDao().getAll()
            .subscribeOn(Schedulers.io()).blockingGet());
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public void sortBy(RecyclerAdapter<Soldier> soldiers,
   Map<Long, Unit> unitMap, int index, boolean asc) {
      Comparator<Soldier> comp;
      if (index == SortBy.mpId.val) {
         comp = Comparator.comparing(soldier -> soldier.mpId);
      } else if (index == SortBy.unitId.val) {
         comp = Comparator.comparing(soldier -> unitMap.get(soldier.unitId).name);
      } else if (index == SortBy.name.val) {
         comp = Comparator.comparing(soldier -> soldier.name);
      } else if (index == SortBy.rank.val) {
         comp = Comparator.comparing(soldier -> soldier.rank);
      } else if (index == SortBy.role.val) {
         comp = Comparator.comparing(soldier -> soldier.role);
      } else return;

      if (!asc) comp = comp.reversed();

      soldiers.sort(comp);
   }

   public void sortBy(List<Soldier> soldiers, Map<Long, Unit> unitMap,
   int index, boolean asc) {
      Comparator<Soldier> comp;
      if (index == SortBy.mpId.val) {
         comp = Comparator.comparing(soldier -> soldier.mpId);
      } else if (index == SortBy.unitId.val) {
         comp = Comparator.comparing(soldier -> unitMap.get(soldier.unitId).name);
      } else if (index == SortBy.name.val) {
         comp = Comparator.comparing(soldier -> soldier.name);
      } else if (index == SortBy.rank.val) {
         comp = Comparator.comparing(soldier -> soldier.rank);
      } else if (index == SortBy.role.val) {
         comp = Comparator.comparing(soldier -> soldier.role);
      } else return;

      if (!asc) comp = comp.reversed();
      soldiers.sort(comp);
   }

   public Completable addSoldier(int mpId, Unit unit, String name, String role, String rank) {
      Soldier soldier = new Soldier(mpId, unit.id, name, rank, role);
      return Completable.fromSingle(KazlamApp.getDatabase().soldiersDao().insert(soldier))
         .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Completable updateSoldier(Soldier soldier, Soldier updated) {
      soldier.mpId = updated.mpId;
      soldier.unitId = updated.unitId;
      soldier.name = updated.name;
      soldier.role = updated.role;
      soldier.rank = updated.rank;

      return KazlamApp.getDatabase().soldiersDao().update(soldier)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread());
   }

   public Completable deleteSoldier(Soldier soldier) {
      return KazlamApp.getDatabase().soldiersDao().delete(soldier)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread());
   }
}
