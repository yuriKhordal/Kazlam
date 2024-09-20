package com.yurikh.kazlam.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.kazlam.KazlamDrawerActivity;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.LeaveRequest;
import com.yurikh.kazlam.viewmodel.LeaveRequestViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LeaveRequestListActivity extends KazlamDrawerActivity {
   LeaveRequestViewModel viewModel;

   Spinner spnSort;
   ImageView btnOrder;
   LinearLayout lytLeaveRequests;

   List<LeaveRequestViewModel.LeaveRequestWrapper> leaveRequests;
   boolean sortAsc = true;

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, LeaveRequestListActivity.class);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_leave_request_list);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      super.loadViews();
      viewModel = new LeaveRequestViewModel(this);
      setTitle(R.string.leave_requests_title);
      lytLeaveRequests = findViewById(R.id.lyt_leave_requests);
      spnSort = findViewById(R.id.spn_sort_by);
      btnOrder = findViewById(R.id.btn_order);

      spnSort.setSelection(LeaveRequestViewModel.SortBy.reason.val);
      spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);
   }

   @Override
   protected void onResume() {
      super.onResume();

      reload();
   }

   private void reload() {
      lytLeaveRequests.removeAllViews();

      int sort = spnSort.getSelectedItemPosition();
      Disposable d = Single.fromCallable(
            () -> viewModel.loadLeaveRequests(sort, sortAsc))
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(list -> {
            leaveRequests = list;
            leaveRequests.forEach(this::addLeaveRequestRow);
         });
   }

   //Events:

   private void addLeaveRequestRow(LeaveRequestViewModel.LeaveRequestWrapper wrap) {
      View row = LayoutInflater.from(lytLeaveRequests.getContext())
         .inflate(R.layout.item_leave_request, lytLeaveRequests, false);

      TextView lblDate = row.findViewById(R.id.txt_date);
      TextView lblReturnDate = row.findViewById(R.id.txt_return_date);
      TextView lblStatus = row.findViewById(R.id.txt_status);
      TextView lblReason = row.findViewById(R.id.lbl_reason);
      TextView lblSoldier = row.findViewById(R.id.lbl_soldier);
      ImageView btnDelete = row.findViewById(R.id.btn_delete);

      lblDate.setText(wrap.request.date);
      lblReturnDate.setText(wrap.request.returnDate);
      lblStatus.setText(wrap.request.status);
      lblReason.setText(wrap.request.reason);
      lblSoldier.setText(wrap.soldier.name);

      row.setTag(wrap.request);
      btnDelete.setTag(wrap);

      row.setOnClickListener(this::noticeClick);
      btnDelete.setOnClickListener(this::btnDeleteClick);

      lytLeaveRequests.addView(row);
   }

   public void btnAddClick(View view) {
      startActivity(LeaveRequestAddUpdateActivity.createIntentAdd(this));
   }

   private void btnDeleteClick(View view) {
      LeaveRequestViewModel.LeaveRequestWrapper wrap =
              (LeaveRequestViewModel.LeaveRequestWrapper)view.getTag();
      int pos = leaveRequests.indexOf(wrap);

      AlertDialog.Builder builder = new AlertDialog.Builder(this)
         .setTitle(R.string.dialog_are_you_sure)
         .setMessage(getString(R.string.dialog_leave_request_delete_msg, wrap.request.reason))
         .setPositiveButton(R.string.dialog_yes, (a, b) -> {
            leaveRequests.remove(pos);
            lytLeaveRequests.removeViewAt(pos);
            viewModel.deleteNotice(wrap.request);
         })
         .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }

   private void noticeClick(View view) {
      LeaveRequest request = (LeaveRequest)view.getTag();
      startActivity(LeaveRequestAddUpdateActivity.createIntentUpdate(this, request.id));
   }

   public void btnOrderClick(View view) {
      sortAsc = !sortAsc;

      if (sortAsc) btnOrder.setImageResource(R.drawable.baseline_arrow_downward_24);
      else btnOrder.setImageResource(R.drawable.baseline_arrow_upward_24);

      int position = spnSort.getSelectedItemPosition();
      viewModel.sortBy(leaveRequests, position, sortAsc);
      lytLeaveRequests.removeAllViews();
      leaveRequests.forEach(this::addLeaveRequestRow);
   }

   AdapterView.OnItemSelectedListener spnSortItemSelectedListener =
      new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (leaveRequests == null) return;

            viewModel.sortBy(leaveRequests, position, sortAsc);
            lytLeaveRequests.removeAllViews();
            leaveRequests.forEach(n -> addLeaveRequestRow(n));
         }
         @Override
         public void onNothingSelected(AdapterView<?> parent) {}
      };

   @Override
   protected boolean navViewOnItemSelected(@NonNull MenuItem item) {
      // Don't actually switch activities when choosing the same activity.
      if (item.getItemId() == R.id.menu_leave_requests) {
         drawerLayout.close();
         reload();
         return false;
      }
      return super.navViewOnItemSelected(item);
   }
}