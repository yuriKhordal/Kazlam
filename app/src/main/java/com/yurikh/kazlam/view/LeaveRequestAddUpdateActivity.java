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
import com.yurikh.kazlam.model.LeaveRequest;
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.viewmodel.LeaveRequestViewModel;

import java.util.Calendar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class LeaveRequestAddUpdateActivity extends AppCompatActivity {
   public static final String INTENT_SOLDIER_ID = "soldier_id";
   public static final String INTENT_UNIT_ID = "unit_id";
   public static final String INTENT_NOTICE_ID = "notice_id";
   public static final String INTENT_ADD_UPDATE = "is_add";

   LeaveRequestViewModel viewModel;
   boolean add;
   LeaveRequest leaveRequest;

   Spinner spnSoldiers;
   TextView txtDate;
   TextView txtReturnDate;
   EditText txtStatus;
   EditText txtReason;
   Button btnAdd;
   Button btnUpdate;
   Button btnCopy;
   Button btnDelete;

   ArrayAdapter<Soldier> soldiers;

   public static Intent createIntentAdd(Context ctx) {
      return new Intent(ctx, LeaveRequestAddUpdateActivity.class)
         .putExtra(INTENT_SOLDIER_ID, -1)
         .putExtra(INTENT_UNIT_ID, -1)
         .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentAddFromUnit(Context ctx, long unitId) {
      return new Intent(ctx, LeaveRequestAddUpdateActivity.class)
              .putExtra(INTENT_SOLDIER_ID, -1)
              .putExtra(INTENT_UNIT_ID, unitId)
              .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentAddFromSoldier(Context ctx, long soldierId) {
      return new Intent(ctx, LeaveRequestAddUpdateActivity.class)
              .putExtra(INTENT_SOLDIER_ID, soldierId)
              .putExtra(INTENT_UNIT_ID, -1)
              .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentUpdate(Context ctx, long noteId) {
      return new Intent(ctx, LeaveRequestAddUpdateActivity.class)
         .putExtra(INTENT_NOTICE_ID, noteId)
         .putExtra(INTENT_ADD_UPDATE, false);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_leave_request_add_update);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      setSupportActionBar(findViewById(R.id.toolbar));
      viewModel = new LeaveRequestViewModel(this);
      add = getIntent().getBooleanExtra(INTENT_ADD_UPDATE, true);

      spnSoldiers = findViewById(R.id.spn_soldiers);
      txtStatus = findViewById(R.id.txt_status);
      txtReason = findViewById(R.id.txt_reason);
      txtDate = findViewById(R.id.txt_date);
      txtReturnDate = findViewById(R.id.txt_return_date);
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
      setTitle(R.string.leave_requests_title);
      btnAdd.setVisibility(View.GONE);

      long id = getIntent().getLongExtra(INTENT_NOTICE_ID, -1);
      if (id == -1) {
         onCreateError();
         return;
      }
      Disposable d = viewModel.getLeaveRequest(id)
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(n -> {
            leaveRequest = n;
            txtReason.setText(leaveRequest.reason);
            txtStatus.setText(leaveRequest.status);
            txtDate.setText(leaveRequest.date);
            txtReturnDate.setText(leaveRequest.returnDate);

            Disposable dd;
            dd = viewModel.getSoldier(leaveRequest.soldierId).subscribe(soldier -> {
               soldiers.add(soldier);
               spnSoldiers.setAdapter(soldiers);
               spnSoldiers.setSelection(0);
               spnSoldiers.setEnabled(false);
            });
         }, ex -> {throw ex;}, this::onCreateError);
   }

   private void onCreateError() {
      setTitle(R.string.err_leave_request_not_exist);
      spnSoldiers.setEnabled(false);
      txtReason.setEnabled(false);
      txtStatus.setEnabled(false);
      txtDate.setEnabled(false);
      txtReturnDate.setEnabled(false);
      btnAdd.setEnabled(false);
      btnUpdate.setEnabled(false);
      btnCopy.setEnabled(false);
      btnDelete.setEnabled(false);

      Toast.makeText(this, R.string.err_leave_request_not_exist, Toast.LENGTH_SHORT).show();
   }

   public boolean isAddMode() { return add; }
   public boolean isUpdateMode() { return !add; }

   // ============================== Events ==============================

   public void txtDateClick(View view) {
      TextView dateView = (TextView) view;

      Calendar c = Calendar.getInstance();

      TimePickerDialog timePicker = new TimePickerDialog(this, (timePicker1, h, m) -> {
         c.set(Calendar.HOUR_OF_DAY, h);
         c.set(Calendar.MINUTE, m);
         String date = Helper.getDateTimeString(c.getTime());
         dateView.setText(date);
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
      String reason = txtReason.getText().toString().trim();
      String date = txtDate.getText().toString().trim();
      String returnDate = txtReturnDate.getText().toString().trim();
      String status = txtStatus.getText().toString().trim();

      if (reason.isEmpty()) {
         Toast.makeText(this, R.string.err_leave_request_empty_reason, Toast.LENGTH_SHORT).show();
         return;
      } else if (date.isEmpty() || date.equals(getString(R.string.leave_request_date))) {
         Toast.makeText(this, R.string.err_leave_request_empty_date, Toast.LENGTH_SHORT).show();
         return;
      } else if (returnDate.isEmpty() || returnDate.equals(getString(R.string.leave_request_return_date))) {
         Toast.makeText(this, R.string.err_leave_request_empty_return_date, Toast.LENGTH_SHORT).show();
         return;
      } else if (soldier == null) {
         Toast.makeText(this, R.string.err_leave_request_empty_soldier, Toast.LENGTH_SHORT).show();
         return;
      }

      if (isAddMode()) {
         Disposable d = viewModel.addLeaveRequest(soldier, date, returnDate,
                         reason, status)
                 .subscribe(this::finish);
      } else {
         Disposable d = viewModel.updateLeaveRequest(leaveRequest, date,
            returnDate, reason, status).subscribe(() -> {
            finish();
            String shortReason = reason.substring(0, 10) + "...";
            String toast = getString(R.string.update_success, shortReason);
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
         });
      }
   }

   public void btnCopyClick(View view) {
      Soldier soldier = (Soldier)spnSoldiers.getSelectedItem();
      String reason = txtReason.getText().toString().trim();
      String date = txtDate.getText().toString().trim();
      String returnDate = txtReturnDate.getText().toString().trim();
      String status = txtStatus.getText().toString().trim();

      if (reason.isEmpty()) {
         Toast.makeText(this, R.string.err_leave_request_empty_reason, Toast.LENGTH_SHORT).show();
         return;
      } else if (date.isEmpty() || date.equals(getString(R.string.leave_request_date))) {
         Toast.makeText(this, R.string.err_leave_request_empty_date, Toast.LENGTH_SHORT).show();
         return;
      } else if (returnDate.isEmpty() || returnDate.equals(getString(R.string.leave_request_return_date))) {
         Toast.makeText(this, R.string.err_leave_request_empty_return_date, Toast.LENGTH_SHORT).show();
         return;
      }

      StringBuilder builder = new StringBuilder();
      builder.append(soldier.name).append(":\n")
         .append(getString(R.string.leave_request_date)).append(": ").append(date).append('\n')
         .append(getString(R.string.leave_request_return_date)).append(": ").append(returnDate).append('\n')
         .append(reason).append('\n');

      if (!status.isEmpty())
         builder.append(getString(R.string.leave_request_status)).append(": ").append(status).append('\n');


      String title = getString(R.string.leave_request);
      ClipboardManager manager =
         (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData data = ClipData.newPlainText(title, builder.toString());
      manager.setPrimaryClip(data);

      title = reason.substring(0, 10) + "...";
      String toast = getString(R.string.copy_success, title);
      Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
   }

   public void btnDeleteClick(View view) {
      if (isAddMode()) return;

      AlertDialog.Builder builder = new AlertDialog.Builder(this)
         .setTitle(R.string.dialog_are_you_sure)
         .setMessage(getString(R.string.dialog_leave_request_delete_msg, leaveRequest.reason))
         .setPositiveButton(R.string.dialog_yes, (a, b) -> {
            viewModel.deleteNotice(leaveRequest);
            finish();
         })
         .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }
}