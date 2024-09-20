package com.yurikh.kazlam.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
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
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.viewmodel.SoldiersViewModel;

import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SoldierListActivity extends KazlamDrawerActivity {
   SoldiersViewModel viewModel;

   Spinner spnSort;
   ImageView btnOrder;
LinearLayout lytSoldiers;
   HorizontalScrollView hscl;

   HashMap<Long, Unit> unitMap;
   List<Soldier> soldiers;
   boolean sortAsc = true;

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, SoldierListActivity.class);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_soldier_list);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      super.loadViews();
      viewModel = new SoldiersViewModel(this);
      setTitle(R.string.soldiers_title);
      lytSoldiers = findViewById(R.id.lyt_soldiers);
      spnSort = findViewById(R.id.spn_sort_by);
      btnOrder = findViewById(R.id.btn_order);

      spnSort.setSelection(SoldiersViewModel.SortBy.name.val);
      spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);

      hscl = (HorizontalScrollView) lytSoldiers.getParent().getParent();
   }

   @Override
   protected void onResume() {
      super.onResume();

      reload();
   }

   private void reload() {
      unitMap = new HashMap<>();
      lytSoldiers.removeAllViews();

      int sort = spnSort.getSelectedItemPosition();
      Disposable d = Single.fromCallable(
         () -> viewModel.loadSoldiers(unitMap, sort, sortAsc))
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

//      tblSoldiers.addView(row);
      lytSoldiers.addView(row);
   }

   public void btnAddClick(View view) {
      startActivity(SoldierAddActivity.createIntent(this));
   }

   private void btnDeleteClick(View view) {
      Soldier soldier = (Soldier)view.getTag();
      int pos = soldiers.indexOf(soldier);

      AlertDialog.Builder builder = new AlertDialog.Builder(this)
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
      startActivity(SoldierActivity.createIntent(this, soldier.id));
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
//         tblSoldiers.addView(rowHeader);
         soldiers.forEach(s -> addSoldierRow(s));
      }
      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
   };

   @Override
   protected boolean navViewOnItemSelected(@NonNull MenuItem item) {
      // Don't actually switch activities when choosing the same activity.
      if (item.getItemId() == R.id.menu_soldiers) {
         drawerLayout.close();
         reload();
         return false;
      }
      return super.navViewOnItemSelected(item);
   }
}