package com.yurikh.kazlam.view;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.kazlam.Helper;
import com.yurikh.kazlam.KazlamDrawerActivity;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.viewmodel.UnitLogViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class UnitLogActivity extends KazlamDrawerActivity {
   public static final String INTENT_LOG_ID = "log_id";

   UnitLogViewModel viewModel;

   TextView lblUnit;
   EditText txtTitle;
   TextView txtDate;
   Button btnDelete;
   Button btnUpdate;
   LinearLayout lytSoldiers;

   UnitLogViewModel.UnitLogWrapper log;

   public static Intent createIntent(Context ctx, long id) {
      return new Intent(ctx, UnitLogActivity.class)
         .putExtra(INTENT_LOG_ID, id);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_unit_log);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      super.loadViews();
      viewModel = new UnitLogViewModel(this);

      lblUnit = findViewById(R.id.lbl_unit);
      txtTitle = findViewById(R.id.txt_title);
      txtDate = findViewById(R.id.txt_date);
      btnUpdate = findViewById(R.id.btn_update);
      btnDelete = findViewById(R.id.btn_delete);
      lytSoldiers = findViewById(R.id.lyt_soldiers);
   }

   @Override
   protected void onResume() {
      super.onResume();

      reload();
   }

   private void reload() {
      lytSoldiers.removeAllViews();

      long id = getIntent().getLongExtra(INTENT_LOG_ID, -1);
      if (id == -1) {
         setOnError();
         return;
      }

      Disposable d = viewModel.getLog(id)
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(this::setViews, err -> setOnError());
   }

   private void setViews(UnitLogViewModel.UnitLogWrapper item) {
      log = item;
      setTitle(item.log.title);
      lblUnit.setText(log.unit.name);
      txtTitle.setText(log.log.title);
      txtDate.setText(log.log.date);

      item.soldiers.forEach(this::addLogSoldierItem);
   }

   private void setOnError() {
      setTitle(R.string.err_unit_log_not_exist);
      txtTitle.setEnabled(false);
      txtDate.setEnabled(false);
      btnDelete.setEnabled(false);
      btnUpdate.setEnabled(false);

      Toast.makeText(this, R.string.err_unit_log_not_exist, Toast.LENGTH_SHORT).show();
   }

   private void addLogSoldierItem(UnitLogViewModel.UnitLogSoldierWrapper item) {
      View row = LayoutInflater.from(lytSoldiers.getContext())
         .inflate(R.layout.item_unit_log_soldier, lytSoldiers, false);

      TextView lblSoldier = row.findViewById(R.id.lbl_soldier);
      TextView lblSoldierId = row.findViewById(R.id.lbl_soldier_id);
      TextView txtValue = row.findViewById(R.id.txt_value);

      lblSoldier.setText(item.soldier.name);
      lblSoldierId.setText(item.soldier.mpId + "");
      txtValue.setText(item.entry.value);

      lytSoldiers.addView(row);
   }

   // ========================= Events =========================

   public void txtDateClick(View view) {
      Calendar c = Calendar.getInstance();

      DatePickerDialog datePicker = new DatePickerDialog(this);
      datePicker.setOnDateSetListener((datePicker1, year, month, day) -> {
         c.set(year, month, day);
         String date = Helper.getDateString(c.getTime());
         txtDate.setText(date);
      });

      datePicker.show();
   }

   public void btnUpdateClick(View view) {
      String title = txtTitle.getText().toString().trim();
      String date = txtDate.getText().toString().trim();
      List<String> values = new ArrayList<>(lytSoldiers.getChildCount());

      if (title.isEmpty()) {
         Toast.makeText(this, R.string.err_unit_log_empty_title, Toast.LENGTH_SHORT).show();
         return;
      }
      if (lytSoldiers.getChildCount() != log.soldiers.size()) {
         Toast.makeText(this, R.string.err_unit_log_wrong_num_children, Toast.LENGTH_SHORT).show();
         return;
      }

      for (int i = 0; i < lytSoldiers.getChildCount(); i++) {
         EditText txtValue = lytSoldiers.getChildAt(i).findViewById(R.id.txt_value);
         String value = txtValue.getText().toString().trim();
         values.add(value);
      }

      Disposable d = viewModel.updateLog(log, title, date, values).subscribe(() -> {
         finish();
         String toast = getString(R.string.update_success, title);
         Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
      });
   }

   public void btnCopyClick(View view) {
      String title = txtTitle.getText().toString().trim();

      if (title.isEmpty()) {
         Toast.makeText(this, R.string.err_unit_log_empty_title, Toast.LENGTH_SHORT).show();
         return;
      }

      StringBuilder builder = new StringBuilder();
      builder.append(txtDate.getText().toString().trim()).append('\n')
         .append(title).append(":\n");

      if (lytSoldiers.getChildCount() != log.soldiers.size()) {
         Toast.makeText(this, R.string.err_unit_log_wrong_num_children, Toast.LENGTH_SHORT).show();
         return;
      }

      for (int i = 0; i < lytSoldiers.getChildCount(); i++) {
         EditText txtValue = lytSoldiers.getChildAt(i).findViewById(R.id.txt_value);
         String value = txtValue.getText().toString().trim();
         String name = log.soldiers.get(i).soldier.name;
         builder.append("- ").append(name).append(": ").append(value).append('\n');
      }

      ClipboardManager manager =
         (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData data = ClipData.newPlainText(title, builder.toString());
      manager.setPrimaryClip(data);

      String toast = getString(R.string.copy_success, title);
      Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
   }

   public void btnDeleteClick(View view) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this)
         .setTitle(R.string.dialog_are_you_sure)
         .setMessage(getString(R.string.dialog_unit_log_delete_msg, log.log.title))
         .setPositiveButton(R.string.dialog_yes, (a, b) -> {
            viewModel.deleteLog(log.log.id).subscribe(this::finish);
         })
         .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }

}