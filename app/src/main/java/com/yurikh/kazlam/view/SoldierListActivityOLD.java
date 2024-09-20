package com.yurikh.kazlam.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yurikh.kazlam.KazlamApp;
import com.yurikh.kazlam.KazlamDrawerActivity;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.RecyclerAdapter;
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.viewmodel.SoldiersViewModel;

import java.util.HashMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SoldierListActivityOLD extends KazlamDrawerActivity {
   SoldiersViewModel viewModel;

   RecyclerView lytSoldiers;
   Spinner spnSort;

   HashMap<Long, Unit> unitMap;
   RecyclerAdapter<Soldier> soldierAdapter;
   boolean sortAsc = true;

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, SoldierListActivityOLD.class);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_soldier_list_old);
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

      spnSort.setSelection(SoldiersViewModel.SortBy.name.val);
      spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);
   }

   @Override
   protected void onResume() {
      super.onResume();

      reload();
   }

   private void reload() {
      unitMap = new HashMap<>();

      int sort = spnSort.getSelectedItemPosition();
      Disposable d = Single.fromCallable(
         () -> viewModel.loadSoldiers(unitMap, sort, sortAsc))
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(soldiers -> {
            soldierAdapter = new RecyclerAdapter<>(soldiers,
               R.layout.item_soldier_old, this::bindSoldierToView);
            lytSoldiers.setLayoutManager(new LinearLayoutManager(this));
            lytSoldiers.setAdapter(soldierAdapter);
         });
   }

   //Events:

   private void bindSoldierToView(RecyclerAdapter.ViewHolder viewHolder, int pos) {
      TextView lblNum = viewHolder.findViewById(R.id.lbl_lineNum);
      TextView lblId = viewHolder.findViewById(R.id.lbl_id);
      TextView lblName = viewHolder.findViewById(R.id.lbl_name);
      TextView lblUnit = viewHolder.findViewById(R.id.lbl_unit);
      TextView lblRole = viewHolder.findViewById(R.id.lbl_role);
      TextView lblRank = viewHolder.findViewById(R.id.lbl_rank);
      ImageView btnDelete = viewHolder.findViewById(R.id.btn_delete);
      Soldier soldier = soldierAdapter.getItem(pos);

      lblNum.setText("" + pos);
      lblId.setText("" + soldier.id);
      lblName.setText(soldier.name);
      lblUnit.setText(unitMap.get(soldier.unitId).name);
      lblRole.setText(soldier.role);
      lblRank.setText(soldier.rank);

      viewHolder.itemView.setTag(soldier);
      btnDelete.setTag(soldier);

      viewHolder.itemView.setOnClickListener(this::soldierClick);
      btnDelete.setOnClickListener(this::btnDeleteClick);
   }

   public void btnAddClick(View view) {
   }

   private void btnDeleteClick(View view) {
      Soldier soldier = (Soldier)view.getTag();
      int pos = soldierAdapter.getPosition(soldier);
      soldierAdapter.remove(soldier);

      Disposable d = KazlamApp.getDatabase().soldiersDao().delete(soldier)
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(() -> soldierAdapter.notifyItemRemoved(pos));
   }

   private void soldierClick(View view) {
      Soldier soldier = (Soldier)view.getTag();

   }

   AdapterView.OnItemSelectedListener spnSortItemSelectedListener =
   new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         if (soldierAdapter == null) return;
         viewModel.sortBy(soldierAdapter, unitMap, position, sortAsc);
         soldierAdapter.notifyDataSetChanged();
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