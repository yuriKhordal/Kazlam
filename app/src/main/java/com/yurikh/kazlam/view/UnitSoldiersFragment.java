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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.viewmodel.SoldiersViewModel;

import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UnitSoldiersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UnitSoldiersFragment extends Fragment {
   private static final String ARG_UNIT_ID = "unit_id";

   SoldiersViewModel viewModel;
   long unitId;

   Spinner spnSort;
   ImageView btnOrder;
LinearLayout lytSoldiers;
   ImageView btnAdd;
   HorizontalScrollView hscl;

   HashMap<Long, Unit> unitMap;
   List<Soldier> soldiers;
   boolean sortAsc = true;

   public UnitSoldiersFragment() {
      // Required empty public constructor
   }

   public static UnitSoldiersFragment newInstance(long unitId) {
      UnitSoldiersFragment fragment = new UnitSoldiersFragment();
      Bundle args = new Bundle();
      args.putLong(ARG_UNIT_ID, unitId);
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (getArguments() == null || !getArguments().containsKey(ARG_UNIT_ID)) {
         Toast.makeText(getContext(), R.string.err_unit_not_exist, Toast.LENGTH_SHORT).show();
         return;
      }
      viewModel = new SoldiersViewModel(getContext());
      unitId = getArguments().getLong(ARG_UNIT_ID);

   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      return inflater.inflate(R.layout.fragment_unit_soldiers, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      lytSoldiers = view.findViewById(R.id.lyt_soldiers);
      spnSort = view.findViewById(R.id.spn_sort_by);
      btnOrder = view.findViewById(R.id.btn_order);
      btnAdd = view.findViewById(R.id.btn_add);

      btnAdd.setOnClickListener(this::btnAddClick);
      btnOrder.setOnClickListener(this::btnOrderClick);

      spnSort.setSelection(SoldiersViewModel.SortBy.name.val);
      spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);

      hscl = (HorizontalScrollView) lytSoldiers.getParent();
   }

   @Override
   public void onResume() {
      super.onResume();

      reload();
   }

   private void reload() {
      unitMap = new HashMap<>();
      lytSoldiers.removeAllViews();
//      tblSoldiers.addView(rowHeader);

      int sort = spnSort.getSelectedItemPosition();
      Disposable d = Single.fromCallable(
                      () -> viewModel.loadUnitSoldiers(unitId, unitMap, sort, sortAsc))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(list -> {
                 soldiers = list;
                 soldiers.forEach(this::addSoldierRow);
              });

      hscl.post(() -> hscl.smoothScrollTo(hscl.getWidth(), 0));
   }

   //Events:

   private void addSoldierRow(Soldier soldier) {
      View row = LayoutInflater.from(lytSoldiers.getContext())
              .inflate(R.layout.item_soldier, lytSoldiers, false);

//      TextView lblNum = row.findViewById(R.id.lbl_lineNum);
      TextView lblId = row.findViewById(R.id.lbl_id);
      TextView lblName = row.findViewById(R.id.lbl_name);
      TextView lblUnit = row.findViewById(R.id.lbl_unit);
      TextView lblRole = row.findViewById(R.id.lbl_role);
      TextView lblRank = row.findViewById(R.id.lbl_rank);
      ImageView btnDelete = row.findViewById(R.id.btn_delete);

//      lblNum.setText("" + soldiers.indexOf(soldier));
      lblId.setText("" + soldier.mpId);
      lblName.setText(soldier.name);
      lblUnit.setText(unitMap.get(soldier.unitId).name);
      lblRole.setText(soldier.role);
      lblRank.setText(soldier.rank);

      row.setTag(soldier);
      btnDelete.setTag(soldier);

      row.setOnClickListener(this::soldierClick);
      btnDelete.setOnClickListener(this::btnDeleteClick);

      lytSoldiers.addView(row);
   }

   public void btnAddClick(View view) {
      startActivity(SoldierAddActivity.createIntent(getContext(), unitId));
   }

   private void btnDeleteClick(View view) {
      Soldier soldier = (Soldier)view.getTag();
      int pos = soldiers.indexOf(soldier);

      AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
              .setTitle(R.string.dialog_are_you_sure)
              .setMessage(getString(R.string.dialog_soldier_delete_msg, soldier.name))
              .setPositiveButton(R.string.dialog_yes, (a, b) -> {
                 soldiers.remove(soldier);
                 lytSoldiers.removeViewAt(pos);
                 viewModel.deleteSoldier(soldier).subscribe();
              })
              .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }

   private void soldierClick(View view) {
      Soldier soldier = (Soldier)view.getTag();
      startActivity(SoldierActivity.createIntent(getContext(), soldier.id));
   }

   public void btnOrderClick(View view) {
      sortAsc = !sortAsc;

      if (sortAsc) btnOrder.setImageResource(R.drawable.baseline_arrow_downward_24);
      else btnOrder.setImageResource(R.drawable.baseline_arrow_upward_24);

      int position = spnSort.getSelectedItemPosition();
      viewModel.sortBy(soldiers, unitMap, position, sortAsc);
      lytSoldiers.removeAllViews();
//      tblSoldiers.addView(rowHeader);
      soldiers.forEach(this::addSoldierRow);
   }

   AdapterView.OnItemSelectedListener spnSortItemSelectedListener =
           new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 if (soldiers == null) return;

                 viewModel.sortBy(soldiers, unitMap, position, sortAsc);
                 lytSoldiers.removeAllViews();
//                 tblSoldiers.addView(rowHeader);
                 soldiers.forEach(s -> addSoldierRow(s));
              }
              @Override
              public void onNothingSelected(AdapterView<?> parent) {}
           };
}