package com.yurikh.kazlam.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.kazlam.KazlamDrawerActivity;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.viewmodel.UnitsViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class UnitListActivity extends KazlamDrawerActivity {
   protected LinearLayout lytUnits;
   UnitsViewModel viewModel;

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, UnitListActivity.class);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_unit_list);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      super.loadViews();
      viewModel = new UnitsViewModel(this);
      lytUnits = findViewById(R.id.lyt_units);
      setTitle(R.string.units_title);
   }

   @Override
   protected void onResume() {
      super.onResume();
      reload();
   }

   private void reload() {
      lytUnits.removeAllViews();
      viewModel.forEachUnitLevel(this::loadUnitViews);
   }

   public void loadUnitViews(Unit unit, int level) {
      View view = LayoutInflater.from(lytUnits.getContext())
         .inflate(R.layout.item_unit, lytUnits, false);
      view.setTag(unit);
      view.setOnClickListener(this::unitViewOnClick);

      TextView lblName = view.findViewById(R.id.lbl_name);
      String indentedName = "\t\t".repeat(level) + unit.name;
      lblName.setText(indentedName);

      View btnDelete = view.findViewById(R.id.btn_delete);
      btnDelete.setTag(unit);
      btnDelete.setOnClickListener(this::btnDeleteOnClick);

      lytUnits.addView(view);
   }

   // Events:
   private void btnDeleteOnClick(View view) {
      Unit unit = (Unit)view.getTag();

      AlertDialog.Builder builder = new AlertDialog.Builder(this)
              .setTitle(R.string.dialog_are_you_sure)
              .setMessage(getString(R.string.dialog_unit_delete_msg, unit.name))
              .setPositiveButton(R.string.dialog_yes, (a, b) -> {
                 Disposable d = viewModel.deleteUnit(unit)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribe(this::reload);
              })
              .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }

   private void unitViewOnClick(View view) {
      Unit unit = (Unit)view.getTag();
      startActivity(UnitActivity.createIntent(this, unit.id));
   }

   public void btnAddClick(View view) {
      startActivity(UnitAddActivity.createIntent(this));
   }

   @Override
   protected boolean navViewOnItemSelected(@NonNull MenuItem item) {
      // Don't actually switch activities when choosing the same activity.
      if (item.getItemId() == R.id.menu_units) {
         drawerLayout.close();
         reload();
         return false;
      }
      return super.navViewOnItemSelected(item);
   }
}