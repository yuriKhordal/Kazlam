package com.yurikh.kazlam.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface LeaveRequestDao {

   @Query("SELECT * FROM LeaveRequests")
   Single<List<LeaveRequest>> getAll();

   @Query("SELECT * FROM LeaveRequests WHERE id=:id")
   Maybe<LeaveRequest> getById(long id);

   @Query("SELECT * FROM LeaveRequests WHERE soldierId=:soldierId")
   Single<List<LeaveRequest>> getBySoldier(long soldierId);

   @Query("SELECT * FROM LeaveRequests WHERE soldierId IN(:soldierIds)")
   Single<List<LeaveRequest>> getBySoldiers(List<Long> soldierIds);

   @Insert
   Single<Long> insert(LeaveRequest request);

   @Update
   Completable update(LeaveRequest request);

   @Delete
   Completable delete(LeaveRequest request);

   @Query("DELETE FROM LeaveRequests WHERE soldierId=:soldierId")
   Completable deleteBySoldier(long soldierId);
}
