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
import com.yurikh.kazlam.model.UnitLog;
import com.yurikh.kazlam.viewmodel.UnitLogViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UnitLogListActivity extends KazlamDrawerActivity {

   UnitLogViewModel viewModel;

   Spinner spnSort;
   ImageView btnOrder;
   LinearLayout lytUnitLogs;

   List<UnitLogViewModel.UnitLogWrapper> logs;
   boolean sortAsc = true;

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, UnitLogListActivity.class);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_unit_log_list);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      super.loadViews();
      viewModel = new UnitLogViewModel(this);
      setTitle(R.string.unit_logs_title);
      lytUnitLogs = findViewById(R.id.lyt_unit_logs);
      spnSort = findViewById(R.id.spn_sort_by);
      btnOrder = findViewById(R.id.btn_order);

      spnSort.setSelection(UnitLogViewModel.SortBy.title.val);
      spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);
   }

   @Override
   protected void onResume() {
      super.onResume();

      reload();
   }

   private void reload() {
      lytUnitLogs.removeAllViews();

      int sort = spnSort.getSelectedItemPosition();
      Disposable d = viewModel.loadLogs(sort, sortAsc)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(list -> {
            logs = list;
            logs.forEach(this::addUnitLogRow);
         });
   }

   // ============================== Events ==============================

   private void addUnitLogRow(UnitLogViewModel.UnitLogWrapper wrapper) {
      View row = LayoutInflater.from(lytUnitLogs.getContext())
         .inflate(R.layout.item_unit_log, lytUnitLogs, false);

      TextView lblUnit = row.findViewById(R.id.lbl_unit);
      TextView lblDate = row.findViewById(R.id.lbl_date);
      TextView lblTitle = row.findViewById(R.id.lbl_title);
      ImageView btnDelete = row.findViewById(R.id.btn_delete);

      lblUnit.setText(wrapper.unit.name);
      lblDate.setText(wrapper.log.date);
      lblTitle.setText(wrapper.log.title);

      row.setTag(wrapper.log);
      btnDelete.setTag(wrapper);

      row.setOnClickListener(this::logClick);
      btnDelete.setOnClickListener(this::btnDeleteClick);

      lytUnitLogs.addView(row);
   }

   public void btnAddClick(View view) {
      startActivity(UnitLogAddActivity.createIntent(this));
   }

   private void btnDeleteClick(View view) {
      UnitLogViewModel.UnitLogWrapper wrapper =
         (UnitLogViewModel.UnitLogWrapper)view.getTag();
      int pos = logs.indexOf(wrapper);

      AlertDialog.Builder builder = new AlertDialog.Builder(this)
         .setTitle(R.string.dialog_are_you_sure)
         .setMessage(getString(R.string.dialog_unit_log_delete_msg, wrapper.log.title))
         .setPositiveButton(R.string.dialog_yes, (a, b) -> {
            logs.remove(pos);
            lytUnitLogs.removeViewAt(pos);
            viewModel.deleteLog(wrapper.log.id).subscribe();
         })
         .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }

   private void logClick(View view) {
      UnitLog log = (UnitLog)view.getTag();
      startActivity(UnitLogActivity.createIntent(this, log.id));
   }

   public void btnOrderClick(View view) {
      sortAsc = !sortAsc;

      if (sortAsc) btnOrder.setImageResource(R.drawable.baseline_arrow_downward_24);
      else btnOrder.setImageResource(R.drawable.baseline_arrow_upward_24);

      int position = spnSort.getSelectedItemPosition();
      viewModel.sortBy(logs, position, sortAsc);
      lytUnitLogs.removeAllViews();
      logs.forEach(this::addUnitLogRow);
   }

   AdapterView.OnItemSelectedListener spnSortItemSelectedListener =
      new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (logs == null) return;

            viewModel.sortBy(logs, position, sortAsc);
            lytUnitLogs.removeAllViews();
            logs.forEach(log -> addUnitLogRow(log));
         }
         @Override
         public void onNothingSelected(AdapterView<?> parent) {}
      };

   @Override
   protected boolean navViewOnItemSelected(@NonNull MenuItem item) {
      // Don't actually switch activities when choosing the same activity.
      if (item.getItemId() == R.id.menu_unit_logs) {
         drawerLayout.close();
         reload();
         return false;
      }
      return super.navViewOnItemSelected(item);
   }
}