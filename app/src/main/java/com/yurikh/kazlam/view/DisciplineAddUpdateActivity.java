package com.yurikh.kazlam.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.kazlam.Helper;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.DisciplinaryNotice;
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.viewmodel.DisciplineViewModel;

import java.util.Calendar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class DisciplineAddUpdateActivity extends AppCompatActivity {
   public static final String INTENT_SOLDIER_ID = "soldier_id";
   public static final String INTENT_UNIT_ID = "unit_id";
   public static final String INTENT_NOTICE_ID = "notice_id";
   public static final String INTENT_ADD_UPDATE = "is_add";

   DisciplineViewModel viewModel;
   boolean add;
   DisciplinaryNotice notice;

   Spinner spnSoldiers;
   EditText txtTitle;
   TextView txtDate;
   EditText txtDescription;
   EditText txtPunishment;
   Button btnAdd;
   Button btnUpdate;
   Button btnCopy;
   Button btnDelete;

   ArrayAdapter<Soldier> soldiers;

   public static Intent createIntentAdd(Context ctx) {
      return new Intent(ctx, DisciplineAddUpdateActivity.class)
         .putExtra(INTENT_SOLDIER_ID, -1)
         .putExtra(INTENT_UNIT_ID, -1)
         .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentAddFromUnit(Context ctx, long unitId) {
      return new Intent(ctx, DisciplineAddUpdateActivity.class)
              .putExtra(INTENT_SOLDIER_ID, -1)
              .putExtra(INTENT_UNIT_ID, unitId)
              .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentAddFromSoldier(Context ctx, long soldierId) {
      return new Intent(ctx, DisciplineAddUpdateActivity.class)
              .putExtra(INTENT_SOLDIER_ID, soldierId)
              .putExtra(INTENT_UNIT_ID, -1)
              .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentAdd(Context ctx, long soldierId) {
      return new Intent(ctx, DisciplineAddUpdateActivity.class)
         .putExtra(INTENT_SOLDIER_ID, soldierId)
         .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentUpdate(Context ctx, long noteId) {
      return new Intent(ctx, DisciplineAddUpdateActivity.class)
         .putExtra(INTENT_NOTICE_ID, noteId)
         .putExtra(INTENT_ADD_UPDATE, false);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_discipline_add_update);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      setSupportActionBar(findViewById(R.id.toolbar));
      viewModel = new DisciplineViewModel(this);
      add = getIntent().getBooleanExtra(INTENT_ADD_UPDATE, true);

      spnSoldiers = findViewById(R.id.spn_soldiers);
      txtTitle = findViewById(R.id.txt_title);
      txtDate = findViewById(R.id.txt_date);
      txtDescription = findViewById(R.id.txt_description);
      txtPunishment = findViewById(R.id.txt_punishment);
      btnAdd = findViewById(R.id.btn_add);
      btnUpdate = findViewById(R.id.btn_update);
      btnCopy = findViewById(R.id.btn_copy);
      btnDelete = findViewById(R.id.btn_delete);

      soldiers = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
      if (isAddMode()) onCreateAdd();
      else onCreateUpdate();
   }

   private void onCreateAdd() {
      setTitle(R.string.add);
      btnUpdate.setVisibility(View.GONE);
      btnDelete.setVisibility(View.GONE);

      long soldierId = getIntent().getLongExtra(INTENT_SOLDIER_ID, -1);
      long unitId = getIntent().getLongExtra(INTENT_UNIT_ID, -1);
      if (soldierId != -1) {
         Disposable d = viewModel.getSoldier(soldierId).subscribe(soldier -> {
            soldiers.add(soldier);
            spnSoldiers.setAdapter(soldiers);
            spnSoldiers.setSelection(0);
            spnSoldiers.setEnabled(false);
         });
      } else if (unitId != -1) {
         Disposable d = viewModel.fillSoldiersAdapter(unitId, soldiers)
                 .subscribe(() -> spnSoldiers.setAdapter(soldiers));
      } else {
         Disposable d = viewModel.fillSoldiersAdapter(soldiers)
                 .subscribe(() -> spnSoldiers.setAdapter(soldiers));
      }
   }

   private void onCreateUpdate() {
      setTitle(R.string.disc_notices_title);
      btnAdd.setVisibility(View.GONE);

      long id = getIntent().getLongExtra(INTENT_NOTICE_ID, -1);
      if (id == -1) {
         onCreateError();
         return;
      }
      Disposable d = viewModel.getNotice(id)
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(n -> {
            notice = n;
            txtTitle.setText(notice.title);
            txtDate.setText(notice.date);
            txtDescription.setText(notice.description);
            txtPunishment.setText(notice.punishment);

            Disposable dd;
            dd = viewModel.getSoldier(notice.soldierId).subscribe(soldier -> {
               soldiers.add(soldier);
               spnSoldiers.setAdapter(soldiers);
               spnSoldiers.setSelection(0);
               spnSoldiers.setEnabled(false);
            });
         }, ex -> {throw ex;}, this::onCreateError);
   }

   private void onCreateError() {
      setTitle(R.string.err_discipline_not_exist);
      spnSoldiers.setEnabled(false);
      txtTitle.setEnabled(false);
      txtDate.setEnabled(false);
      txtDescription.setEnabled(false);
      txtPunishment.setEnabled(false);
      btnAdd.setEnabled(false);
      btnUpdate.setEnabled(false);
      btnCopy.setEnabled(false);
      btnDelete.setEnabled(false);

      Toast.makeText(this, R.string.err_discipline_not_exist, Toast.LENGTH_SHORT).show();
   }

   public boolean isAddMode() { return add; }
   public boolean isUpdateMode() { return !add; }

   // ============================== Events ==============================

   public void txtDateClick(View view) {
      Calendar c = Calendar.getInstance();

      TimePickerDialog timePicker = new TimePickerDialog(this, (timePicker1, h, m) -> {
         c.set(Calendar.HOUR_OF_DAY, h);
         c.set(Calendar.MINUTE, m);
         String date = Helper.getDateTimeString(c.getTime());
         txtDate.setText(date);
      }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);

      DatePickerDialog datePicker = new DatePickerDialog(this);
      datePicker.setOnDateSetListener((datePicker1, year, month, day) -> {
         c.set(year, month, day);
         timePicker.show();
      });

      datePicker.show();
   }

   public void btnAddUpdateClick(View view) {
      Soldier soldier = (Soldier)spnSoldiers.getSelectedItem();
      String title = txtTitle.getText().toString().trim();
      String date = txtDate.getText().toString().trim();
      String description = txtDescription.getText().toString();
      String punishment = txtPunishment.getText().toString();

      if (title.isEmpty()) {
         Toast.makeText(this, R.string.err_discipline_empty_title, Toast.LENGTH_SHORT).show();
         return;
      } else if (date.isEmpty() || date.equals(getString(R.string.disc_date))) {
         Toast.makeText(this, R.string.err_discipline_empty_date, Toast.LENGTH_SHORT).show();
         return;
      } else if (soldier == null) {
         Toast.makeText(this, R.string.err_discipline_empty_soldier, Toast.LENGTH_SHORT).show();
         return;
      }

      if (isAddMode()) {
         Disposable d = viewModel.addNotice(soldier, title, date, description, punishment)
            .subscribe(this::finish);
      } else {
         Disposable d = viewModel.updateNotice(notice, title, date,
            description, punishment).subscribe(() -> {
            finish();
            String toast = getString(R.string.update_success, title);
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
         });
      }
   }

   public void btnCopyClick(View view) {
      Soldier soldier = (Soldier)spnSoldiers.getSelectedItem();
      String date = txtDate.getText().toString().trim();
      String title = txtTitle.getText().toString().trim();
      String description = txtDescription.getText().toString();
      String punishment = txtPunishment.getText().toString();

      if (title.isEmpty()) {
         Toast.makeText(this, R.string.err_discipline_empty_title, Toast.LENGTH_SHORT).show();
         return;
      } else if (date.isEmpty() || date.equals(getString(R.string.disc_date))) {
         Toast.makeText(this, R.string.err_discipline_empty_date, Toast.LENGTH_SHORT).show();
         return;
      }

      StringBuilder builder = new StringBuilder();
      builder.append(date).append(", ").append(soldier.name).append('\n')
         .append(title).append(":\n");

      if (!description.isEmpty())
         builder.append(description).append('\n');
      if (!punishment.isEmpty())
         builder.append(getText(R.string.disc_punishment)).append(": ").append(punishment).append('\n');

      ClipboardManager manager =
         (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData data = ClipData.newPlainText(title, builder.toString());
      manager.setPrimaryClip(data);

      String toast = getString(R.string.copy_success, title);
      Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
   }

   public void btnDeleteClick(View view) {
      if (isAddMode()) return;

      AlertDialog.Builder builder = new AlertDialog.Builder(this)
         .setTitle(R.string.dialog_are_you_sure)
         .setMessage(getString(R.string.dialog_discipline_delete_msg, notice.title))
         .setPositiveButton(R.string.dialog_yes, (a, b) -> {
            viewModel.deleteNotice(notice);
            finish();
         })
         .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }
}