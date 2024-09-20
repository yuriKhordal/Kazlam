package com.yurikh.kazlam.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;

import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.UnitTree;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public abstract class ComplexUnitDao implements UnitDao {
   /**
    * Gets the unit with all of it's subunit(recursively) by id.
    * @param id The id of the mother unit.
    */
   public Single<List<Unit>> getWithAllChildren(long id) {
      return Single.fromCallable(() -> {
         UnitTree tree = new UnitTree();
         tree.fill(id);
         return tree.flattenUnits();
      });
   }

   /**
    * Delete a unit safely(together with soldiers and subunits)
    * @param unit The unit to delete.
    */
   public Completable delete(Unit unit) {
      return Completable.fromAction(() -> deleteSync(unit.id));
   }

   /**
    * Delete a unit safely(together with soldiers and subunits)
    * @param id The id of the unit to delete.
    */
   public Completable delete(long id) {
      return Completable.fromAction(() -> deleteSync(id));
   }

   private void deleteSync(long id) {
      UnitTree tree = new UnitTree();
      tree.fill(id);
      tree.fillSoldiers();

      List<Long> unitIds = tree.flattenUnits().stream()
         .map(unit -> unit.id).collect(Collectors.toList());
      unsetCommanders(unitIds);

      KazlamApp.getDatabase().soldiersDao().delete(tree.flattenSoldiers()).blockingAwait();
      for (UnitTree.Node node : tree.postOrder())
         KazlamApp.getDatabase().unitsDao().deleteInternal(node.parent).blockingAwait();
   }

   @Query("UPDATE Units SET commanderId=null WHERE id IN(:ids)")
   protected abstract void unsetCommanders(List<Long> ids);

   @Delete
   protected abstract Completable deleteInternal(Unit unit);
}
