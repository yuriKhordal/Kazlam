package com.yurikh.kazlam.viewmodel;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.UnitTree;
import com.yurikh.kazlam.model.DisciplinaryNotice;
import com.yurikh.kazlam.model.Soldier;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DisciplineViewModel {
   // The names are the names of the columns, the values are the indicies
   // in the `disciplinary_sortby` array
   public enum SortBy {
      soldierId(0), title(1), date(2);
      public final int val;
      SortBy(int val) { this.val = val; }
      public static SortBy fromInt(int val) {
         switch (val) {
            case 0: return soldierId;
            case 1: return title;
            case 2: return date;
            default: throw new IllegalArgumentException("Can't convert (" + val + ") to " + SortBy.class);
         }
      }
   }

   public static class DisciplinaryNoticeWrapper {
      public DisciplinaryNotice notice;
      public Soldier soldier;

      public DisciplinaryNoticeWrapper(DisciplinaryNotice notice, Soldier soldier) {
         this.notice = notice;
         this.soldier = soldier;
      }
   }

   Context ctx;

   public DisciplineViewModel(Context ctx) {
      this.ctx = ctx;
   }

   public Maybe<DisciplinaryNotice> getNotice(long id) {
      return KazlamApp.getDatabase().disciplinaryDao().getById(id)
         .subscribeOn(Schedulers.io());
   }

   public List<DisciplinaryNoticeWrapper> loadNotices(int sortBy, boolean asc) {
      List<Soldier> soldiers = KazlamApp.getDatabase().soldiersDao()
              .getAll().blockingGet();
      List<DisciplinaryNotice> notices = KazlamApp.getDatabase().disciplinaryDao()
              .getAll().blockingGet();

      Map<Long, Soldier> soldierMap = new HashMap<>();
      soldiers.forEach(soldier -> soldierMap.put(soldier.id, soldier));

      List<DisciplinaryNoticeWrapper> noticeWrappers = notices.stream()
              .map(notice -> new DisciplinaryNoticeWrapper(notice, soldierMap.get(notice.soldierId)))
              .collect(Collectors.toList());
      sortBy(noticeWrappers, sortBy, asc);

      return noticeWrappers;
   }

   public List<DisciplinaryNoticeWrapper> loadUnitNotices(long unitId, int sortBy, boolean asc) {
      UnitTree tree = new UnitTree();
      tree.fill(unitId);
      tree.fillSoldiers();

      List<Soldier> soldiers = tree.flattenSoldiers();
      List<DisciplinaryNotice> notices = KazlamApp.getDatabase().disciplinaryDao()
              .getBySoldiers(soldiers.stream().map(s -> s.id).collect(Collectors.toList()))
              .blockingGet();

      Map<Long, Soldier> soldierMap = new HashMap<>();
      soldiers.forEach(soldier -> soldierMap.put(soldier.id, soldier));

      List<DisciplinaryNoticeWrapper> noticeWrappers = notices.stream()
              .map(notice -> new DisciplinaryNoticeWrapper(notice, soldierMap.get(notice.soldierId)))
              .collect(Collectors.toList());
      sortBy(noticeWrappers, sortBy, asc);

      return noticeWrappers;
   }

   public List<DisciplinaryNoticeWrapper> loadSoldierNotices(long soldierId, int sortBy, boolean asc) {
      Soldier soldier = getSoldier(soldierId).blockingGet();
      List<DisciplinaryNotice> notices = KazlamApp.getDatabase().disciplinaryDao()
              .getBySoldier(soldierId).blockingGet();

      List<DisciplinaryNoticeWrapper> noticeWrappers = notices.stream()
              .map(notice -> new DisciplinaryNoticeWrapper(notice, soldier))
              .collect(Collectors.toList());
      sortBy(noticeWrappers, sortBy, asc);

      return noticeWrappers;
   }

   public Completable fillSoldiersAdapter(ArrayAdapter<Soldier> adapter) {
      return Completable.fromAction(() -> {
         adapter.addAll(KazlamApp.getDatabase().soldiersDao().getAll()
                 .subscribeOn(Schedulers.io()).blockingGet());
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Completable fillSoldiersAdapter(long unitId, ArrayAdapter<Soldier> adapter) {
      return Completable.fromAction(() -> {
         UnitTree tree = new UnitTree();
         tree.fill(unitId);
         tree.fillSoldiers();

         List<Soldier> soldiers = tree.flattenSoldiers();
         adapter.addAll(soldiers);
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public Single<Soldier> getSoldier(long id) {
      return Single.fromMaybe(KazlamApp.getDatabase().soldiersDao().getById(id))
         .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
   }

   public void sortBy(List<DisciplinaryNoticeWrapper> notices, int index, boolean asc) {
      Comparator<DisciplinaryNoticeWrapper> comp;
      if (index == SortBy.soldierId.val) {
         comp = Comparator.comparing(wrap -> wrap.soldier.name);
      } else if (index == SortBy.title.val) {
         comp = Comparator.comparing(wrap -> wrap.notice.title);
      } else if (index == SortBy.date.val) {
         comp = Comparator.comparing(wrap -> wrap.notice.date);
      } else return;

      if (!asc) comp = comp.reversed();
      notices.sort(comp);
   }

   public Completable addNotice(Soldier soldier, String title, String date, String desc, String punishment) {
      DisciplinaryNotice notice = new DisciplinaryNotice(
         soldier.id, date, title, desc, punishment
      );

      Single<Long> insert = KazlamApp.getDatabase().disciplinaryDao().insert(notice);
      return Completable.fromSingle(insert)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread());
   }

   public Completable updateNotice(DisciplinaryNotice notice, String title, String date, String desc, String punishment) {
      notice.title = title;
      notice.description = desc;
      notice.date = date;
      notice.punishment = punishment;

      return KazlamApp.getDatabase().disciplinaryDao().update(notice)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread());
   }

   public void deleteNotice(DisciplinaryNotice notice) {
      KazlamApp.getDatabase().disciplinaryDao().delete(notice)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe();
   }
}
