package com.yurikh.kazlam.viewmodel;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.UnitTree;
import com.yurikh.kazlam.model.LeaveRequest;
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

public class LeaveRequestViewModel {
   // The names are the names of the columns, the values are the indicies
   // in the `leave_request_sort_by` array
   public enum SortBy {
      soldierId(0), reason(1), date(2), returnDate(3);
      public final int val;
      SortBy(int val) { this.val = val; }
      public static SortBy fromInt(int val) {
         switch (val) {
            case 0: return soldierId;
            case 1: return reason;
            case 2: return date;
            case 3: return returnDate;
            default: throw new IllegalArgumentException("Can't convert (" + val + ") to " + SortBy.class);
         }
      }
   }

   public static class LeaveRequestWrapper {
      public LeaveRequest request;
      public Soldier soldier;

      public LeaveRequestWrapper(LeaveRequest request, Soldier soldier) {
         this.request = request;
         this.soldier = soldier;
      }
   }

   Context ctx;

   public LeaveRequestViewModel(Context ctx) {
      this.ctx = ctx;
   }

   public Maybe<LeaveRequest> getLeaveRequest(long id) {
      return KazlamApp.getDatabase().leaveRequestDao().getById(id)
         .subscribeOn(Schedulers.io());
   }

   public List<LeaveRequestWrapper> loadLeaveRequests(int sortBy, boolean asc) {
      List<Soldier> soldiers = KazlamApp.getDatabase().soldiersDao()
              .getAll().blockingGet();
      List<LeaveRequest> requests = KazlamApp.getDatabase().leaveRequestDao()
              .getAll().blockingGet();

      Map<Long, Soldier> soldierMap = new HashMap<>();
      soldiers.forEach(soldier -> soldierMap.put(soldier.id, soldier));

      List<LeaveRequestWrapper> wrappers = requests.stream()
              .map(request -> new LeaveRequestWrapper(request, soldierMap.get(request.soldierId)))
              .collect(Collectors.toList());

      sortBy(wrappers, sortBy, asc);

      return wrappers;
   }

   public List<LeaveRequestWrapper> loadLeaveRequestsByUnit(long unitId, int sortBy, boolean asc) {
      UnitTree tree = new UnitTree();
      tree.fill(unitId);
      tree.fillSoldiers();

      List<Soldier> soldiers = tree.flattenSoldiers();
      List<LeaveRequest> requests = KazlamApp.getDatabase().leaveRequestDao()
              .getBySoldiers(soldiers.stream().map(s -> s.id).collect(Collectors.toList()))
              .blockingGet();

      Map<Long, Soldier> soldierMap = new HashMap<>();
      soldiers.forEach(soldier -> soldierMap.put(soldier.id, soldier));

      List<LeaveRequestWrapper> wrappers = requests.stream()
              .map(request -> new LeaveRequestWrapper(request, soldierMap.get(request.soldierId)))
              .collect(Collectors.toList());

      sortBy(wrappers, sortBy, asc);

      return wrappers;
   }

   public List<LeaveRequestWrapper> loadLeaveRequestsBySoldier(long soldierId, int sortBy, boolean asc) {
      Soldier soldier = getSoldier(soldierId).blockingGet();
      List<LeaveRequest> requests = KazlamApp.getDatabase().leaveRequestDao()
              .getBySoldier(soldierId).blockingGet();

      List<LeaveRequestWrapper> wrappers = requests.stream()
              .map(request -> new LeaveRequestWrapper(request, soldier))
              .collect(Collectors.toList());

      sortBy(wrappers, sortBy, asc);

      return wrappers;
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

   public void sortBy(List<LeaveRequestWrapper> requests, int index, boolean asc) {
      Comparator<LeaveRequestWrapper> comp;
      if (index == SortBy.soldierId.val) {
         comp = Comparator.comparing(wrap -> wrap.soldier.name);
      } else if (index == SortBy.reason.val) {
         comp = Comparator.comparing(wrap -> wrap.request.reason);
      } else if (index == SortBy.date.val) {
         comp = Comparator.comparing(wrap -> wrap.request.date);
      } else if (index == SortBy.returnDate.val) {
         comp = Comparator.comparing(wrap -> wrap.request.returnDate);
      } else return;

      if (!asc) comp = comp.reversed();
      requests.sort(comp);
   }

   public Completable addLeaveRequest(Soldier soldier, String date,
   String returnDate, String reason, String status) {
      LeaveRequest request = new LeaveRequest(
         soldier.id, date, returnDate, reason, status
      );

      Single<Long> insert = KazlamApp.getDatabase().leaveRequestDao().insert(request);
      return Completable.fromSingle(insert)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread());
   }

   public Completable updateLeaveRequest(LeaveRequest request, String date,
   String returnDate, String reason, String status) {
      request.date = date;
      request.returnDate = returnDate;
      request.reason = reason;
      request.status = status;

      return KazlamApp.getDatabase().leaveRequestDao().update(request)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread());
   }

   public void deleteNotice(LeaveRequest notice) {
      KazlamApp.getDatabase().leaveRequestDao().delete(notice)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe();
   }
}
