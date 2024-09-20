package com.yurikh.kazlam.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.kazlam.Helper;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.viewmodel.UnitLogViewModel;

import java.util.Calendar;

import io.reactivex.rxjava3.disposables.Disposable;

public class UnitLogAddActivity extends AppCompatActivity {
   public static final String INTENT_UNIT_ID = "unit_id";

   UnitLogViewModel viewModel;

   Spinner spnUnit;
   EditText txtTitle;
   TextView txtDate;

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, UnitLogAddActivity.class)
         .putExtra(INTENT_UNIT_ID, -1);
   }

   public static Intent createIntent(Context ctx, long unitId) {
      return new Intent(ctx, UnitLogAddActivity.class)
         .putExtra(INTENT_UNIT_ID, unitId);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_unit_log_add);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      setSupportActionBar(findViewById(R.id.toolbar));
      setTitle(R.string.add);

      viewModel = new UnitLogViewModel(this);
      spnUnit = findViewById(R.id.spn_unit);
      txtTitle = findViewById(R.id.txt_title);
      txtDate = findViewById(R.id.txt_date);

      txtDate.setText(Helper.getDateString());

      long unitId = getIntent().getLongExtra(INTENT_UNIT_ID, -1);
      ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(
         this, android.R.layout.simple_spinner_dropdown_item
      );
      Disposable d = viewModel.getUnit(unitId).subscribe(unit -> {
         unitAdapter.add(unit);
         spnUnit.setAdapter(unitAdapter);
         spnUnit.setEnabled(false);
      }, error -> {
         Disposable dd = viewModel.fillUnitAdapter(unitAdapter)
            .subscribe(() -> spnUnit.setAdapter(unitAdapter));
      });
   }

   // ========================= Events =========================

   public void btnAddClick(View view) {
      Unit unit = (Unit) spnUnit.getSelectedItem();
      String title = txtTitle.getText().toString().trim();
      String date = txtDate.getText().toString().trim();

      if (title.isEmpty()) {
         Toast.makeText(this, R.string.err_unit_log_empty_title, Toast.LENGTH_SHORT).show();
         return;
      } else if (unit == null) {
         Toast.makeText(this, R.string.err_unit_log_empty_unit, Toast.LENGTH_SHORT).show();
         return;
      }

      Disposable d = viewModel.addLog(unit.id, title, date).subscribe(id -> {
         finish();
         startActivity(UnitLogActivity.createIntent(this, id));
      });
   }

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
}