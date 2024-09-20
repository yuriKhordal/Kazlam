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

import com.yurikh.kazlam.Helper;
import com.yurikh.kazlam.KazlamDrawerActivity;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.DisciplinaryNotice;
import com.yurikh.kazlam.viewmodel.DisciplineViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DisciplineListActivity extends KazlamDrawerActivity {
   DisciplineViewModel viewModel;

   Spinner spnSort;
   ImageView btnOrder;
   LinearLayout lytDisciplinaryNotices;

   List<DisciplineViewModel.DisciplinaryNoticeWrapper> disciplinaries;
   boolean sortAsc = true;

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, DisciplineListActivity.class);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_discipline_list);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      super.loadViews();
      viewModel = new DisciplineViewModel(this);
      setTitle(R.string.disc_notices_title);
      lytDisciplinaryNotices = findViewById(R.id.lyt_disciplinary_notices);
      spnSort = findViewById(R.id.spn_sort_by);
      btnOrder = findViewById(R.id.btn_order);

      spnSort.setSelection(DisciplineViewModel.SortBy.title.val);
      spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);
   }

   @Override
   protected void onResume() {
      super.onResume();

      reload();
   }

   private void reload() {
      lytDisciplinaryNotices.removeAllViews();

      int sort = spnSort.getSelectedItemPosition();
      Disposable d = Single.fromCallable(
            () -> viewModel.loadNotices(sort, sortAsc))
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(list -> {
            disciplinaries = list;
            disciplinaries.forEach(this::addNoticeRow);
         });
   }

   //Events:

   private void addNoticeRow(DisciplineViewModel.DisciplinaryNoticeWrapper wrapper) {
      View row = LayoutInflater.from(lytDisciplinaryNotices.getContext())
         .inflate(R.layout.item_disciplinary_notice, lytDisciplinaryNotices, false);

      TextView lblSoldier = row.findViewById(R.id.lbl_soldier);
      TextView lblTitle = row.findViewById(R.id.lbl_title);
      View dividerDescription = row.findViewById(R.id.divider_description);
      TextView lblDescription = row.findViewById(R.id.lbl_description);
      View dividerPunishment = row.findViewById(R.id.divider_punishment);
      TextView lblPunishment = row.findViewById(R.id.lbl_punishment);
      TextView lblDate = row.findViewById(R.id.lbl_date);
      ImageView btnDelete = row.findViewById(R.id.btn_delete);

      lblSoldier.setText(wrapper.soldier.name);
      lblTitle.setText(wrapper.notice.title);
      lblDate.setText(wrapper.notice.date);

      if (Helper.StringEmpty(wrapper.notice.description)) {
         dividerDescription.setVisibility(View.GONE);
         lblDescription.setVisibility(View.GONE);
      } else {
         lblDescription.setText(wrapper.notice.description);
      }
      if (Helper.StringEmpty(wrapper.notice.punishment)) {
         dividerPunishment.setVisibility(View.GONE);
         lblPunishment.setVisibility(View.GONE);
      } else {
         lblPunishment.setText(wrapper.notice.punishment);
      }

      row.setTag(wrapper.notice);
      btnDelete.setTag(wrapper);

      row.setOnClickListener(this::noticeClick);
      btnDelete.setOnClickListener(this::btnDeleteClick);

      lytDisciplinaryNotices.addView(row);
   }

   public void btnAddClick(View view) {
      startActivity(DisciplineAddUpdateActivity.createIntentAdd(this));
   }

   private void btnDeleteClick(View view) {
      DisciplineViewModel.DisciplinaryNoticeWrapper wrapper =
              (DisciplineViewModel.DisciplinaryNoticeWrapper)view.getTag();
      int pos = disciplinaries.indexOf(wrapper);

      AlertDialog.Builder builder = new AlertDialog.Builder(this)
         .setTitle(R.string.dialog_are_you_sure)
         .setMessage(getString(R.string.dialog_discipline_delete_msg, wrapper.notice.title))
         .setPositiveButton(R.string.dialog_yes, (a, b) -> {
            disciplinaries.remove(pos);
            lytDisciplinaryNotices.removeViewAt(pos);
            viewModel.deleteNotice(wrapper.notice);
         })
         .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }

   private void noticeClick(View view) {
      DisciplinaryNotice notice = (DisciplinaryNotice)view.getTag();
      startActivity(DisciplineAddUpdateActivity.createIntentUpdate(this, notice.id));
   }

   public void btnOrderClick(View view) {
      sortAsc = !sortAsc;

      if (sortAsc) btnOrder.setImageResource(R.drawable.baseline_arrow_downward_24);
      else btnOrder.setImageResource(R.drawable.baseline_arrow_upward_24);

      int position = spnSort.getSelectedItemPosition();
      viewModel.sortBy(disciplinaries, position, sortAsc);
      lytDisciplinaryNotices.removeAllViews();
      disciplinaries.forEach(this::addNoticeRow);
   }

   AdapterView.OnItemSelectedListener spnSortItemSelectedListener =
      new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (disciplinaries == null) return;

            viewModel.sortBy(disciplinaries, position, sortAsc);
            lytDisciplinaryNotices.removeAllViews();
            disciplinaries.forEach(n -> addNoticeRow(n));
         }
         @Override
         public void onNothingSelected(AdapterView<?> parent) {}
      };

   @Override
   protected boolean navViewOnItemSelected(@NonNull MenuItem item) {
      // Don't actually switch activities when choosing the same activity.
      if (item.getItemId() == R.id.menu_discipline) {
         drawerLayout.close();
         reload();
         return false;
      }
      return super.navViewOnItemSelected(item);
   }
}