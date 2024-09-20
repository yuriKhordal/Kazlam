package com.yurikh.kazlam.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.UnitLog;
import com.yurikh.kazlam.viewmodel.UnitLogViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UnitLogsFragment extends Fragment {
   private static final String ARG_UNIT_ID = "unit_id";

   long unitId;
   UnitLogViewModel viewModel;

   Spinner spnSort;
   ImageView btnOrder;
   LinearLayout lytUnitLogs;
   ImageView btnAdd;

   List<UnitLogViewModel.UnitLogWrapper> logs;
   boolean sortAsc = true;

   public UnitLogsFragment() {
      // Required empty public constructor
   }

   public static UnitLogsFragment newInstance(long unitId) {
      UnitLogsFragment fragment = new UnitLogsFragment();
      Bundle args = new Bundle();
      args.putLong(ARG_UNIT_ID, unitId);
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (getArguments() != null) {
         unitId = getArguments().getLong(ARG_UNIT_ID, -1);
      }
      viewModel = new UnitLogViewModel(getContext());
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      return inflater.inflate(R.layout.fragment_unit_logs, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      lytUnitLogs = view.findViewById(R.id.lyt_unit_logs);
      spnSort = view.findViewById(R.id.spn_sort_by);
      btnOrder = view.findViewById(R.id.btn_order);
      btnAdd = view.findViewById(R.id.btn_add);

      btnOrder.setOnClickListener(this::btnOrderClick);
      btnAdd.setOnClickListener(this::btnAddClick);

      spnSort.setSelection(UnitLogViewModel.SortBy.title.val);
      spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);
   }

   @Override
   public void onResume() {
      super.onResume();

      reload();
   }

   private void reload() {
      lytUnitLogs.removeAllViews();

      int sort = spnSort.getSelectedItemPosition();
      Disposable d = viewModel.loadLogsByUnit(unitId, sort, sortAsc)
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

   private void btnAddClick(View view) {
      startActivity(UnitLogAddActivity.createIntent(getContext(), unitId));
   }

   private void btnDeleteClick(View view) {
      UnitLogViewModel.UnitLogWrapper wrapper =
         (UnitLogViewModel.UnitLogWrapper)view.getTag();
      int pos = logs.indexOf(wrapper);

      AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
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
      startActivity(UnitLogActivity.createIntent(getContext(), log.id));
   }

   private void btnOrderClick(View view) {
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
}