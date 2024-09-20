package com.yurikh.kazlam.viewmodel;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.yurikh.kazlam.Helper;
import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.UnitTree;
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.model.UnitDao;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Triple;

public class UnitsViewModel {
   private final Context ctx;
   private final Unit none;

   public UnitsViewModel(Context ctx) {
      this.ctx = ctx;
      none = new Unit(ctx.getString(R.string.unit_none));
   }

   public void forEachUnitLevel(BiConsumer<Unit, Integer> func) {
      Disposable d = UnitTree.globalTreeAsync()
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(tree -> {
                 tree.sort();
                 tree.root.children.forEach(node -> forEachUnitLevel(node, 0, func));
              });
   }

   public void forEachSubUnitLevel(long unitId, BiConsumer<Unit, Integer> func) {
      UnitTree tree = new UnitTree();
      Disposable d = tree.fillAsync(unitId)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(() -> {
                 tree.sort();
                 tree.root.children.forEach(node -> forEachUnitLevel(node, 0, func));
              });
   }

   private void forEachUnitLevel(UnitTree.Node node, int pos, BiConsumer<Unit, Integer> func) {
      func.accept(node.parent, pos);
      node.children.forEach(child -> forEachUnitLevel(child, pos+1, func));
   }

   public Single<ArrayAdapter<Unit>> getUnitsAdapter(int layout) {
      return Single.fromCallable(() -> {
         List<Unit> units = KazlamApp.getDatabase().unitsDao().getAll().blockingGet();
         ArrayAdapter<Unit> adapter = new ArrayAdapter<>(ctx, layout);
         adapter.add(none);
         adapter.addAll(units);
         return adapter;
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Triple<Unit, Unit, Soldier> initialize(long id,
   ArrayAdapter<Unit> mothersAdapter,
   ArrayAdapter<Soldier> commandersAdapter) {
      UnitDao unitDao = KazlamApp.getDatabase().unitsDao();
      Unit unit, mother;
      Soldier commander;

      unit = unitDao.getById(id).blockingGet();
      if (unit == null) {
         mother = null;
         commander = null;
         return new Triple<>(null, null, null);
      }

      UnitTree tree = new UnitTree();
      tree.fill(id);
      tree.fillSoldiers();
      tree.sort();

      // Get possible mother units:
      List<Unit> mothersList = unitDao.getAll().blockingGet();
      mothersList.sort(Comparator.comparing(u -> u.name));

      // Remove daughter units from mothersList to avoid loop.
      tree.postOrder().forEach(node -> mothersList.remove(
         Collections.binarySearch(mothersList, node.parent,
            Comparator.comparing(u -> u.name))
      ));

      Unit noneUnit = new Unit(-1, null, null, ctx.getString(R.string.spinner_none));
      mothersAdapter.add(noneUnit);
      mothersAdapter.addAll(mothersList);

      // Get possible commanders:
      List<Soldier> soldierList = tree.flattenSoldiers();
      soldierList.sort(Comparator.comparing(s -> s.name));

      Soldier noneSoldier = new Soldier(-1, -1, ctx.getString(R.string.spinner_none));
      commandersAdapter.add(noneSoldier);
      commandersAdapter.addAll(soldierList);

      // Fill members:
      if (unit.motherUnitId == null)
         mother = null;
      else mother = mothersList.get(Helper.searchByKey(
         mothersList, unit.motherUnitId, u -> u.id
      ));

      if (unit.commanderId == null)
         commander = null;
      else commander = soldierList.get(Helper.searchByKey(
         soldierList, unit.commanderId, u -> u.id
      ));

      return new Triple<>(unit, mother, commander);
   }

   public void addUnit(String name, Unit mother) {
      Unit unit = new Unit(name);
      unit.motherUnitId = (mother == null || mother == none) ? null : mother.id;
      unit.commanderId = null;

      KazlamApp.getDatabase().unitsDao().insert(unit)
         .subscribeOn(Schedulers.io())
         .blockingSubscribe();
   }

   public Completable updateUnit(Unit unit, String name, Unit mother, Soldier commander) {
      unit.name = name;
      unit.motherUnitId = (mother == null) ? null : mother.id;
      unit.commanderId = (commander == null) ? null : commander.id;

      return KazlamApp.getDatabase().unitsDao().update(unit)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread());
   }

   public Completable deleteUnit(Unit unit) {
      return KazlamApp.getDatabase().unitsDao().delete(unit)
         .subscribeOn(Schedulers.io());
   }
}
