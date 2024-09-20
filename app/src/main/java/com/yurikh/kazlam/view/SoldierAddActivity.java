package com.yurikh.kazlam.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.viewmodel.SoldiersViewModel;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

public class SoldierAddActivity extends AppCompatActivity {
   public static final String INTENT_UNIT_ID = "unitId";

   SoldiersViewModel viewModel;

   EditText txtMpId;
   EditText txtName;
   AutoCompleteTextView txtRole;
   AutoCompleteTextView txtRank;
   Spinner spnUnit;

   ArrayAdapter<String> roles;
   ArrayAdapter<String> ranks;
   ArrayAdapter<Unit> units;

   public static Intent createIntent(Context ctx, long unitId) {
      return new Intent(ctx, SoldierAddActivity.class)
              .putExtra(INTENT_UNIT_ID, unitId);
   }

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, SoldierAddActivity.class);
   }

   // ============================== Events ==============================

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_soldier_add);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      setSupportActionBar(findViewById(R.id.toolbar));
      viewModel = new SoldiersViewModel(this);

      setTitle(R.string.add);

      txtMpId = findViewById(R.id.txt_mp_id);
      txtName = findViewById(R.id.txt_name);
      txtRole = findViewById(R.id.txt_role);
      txtRank = findViewById(R.id.txt_rank);
      spnUnit = findViewById(R.id.spn_unit);

      long unitId = getIntent().getLongExtra(INTENT_UNIT_ID, -1);

      roles = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
      ranks = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
      units = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

      Disposable d;
      d = viewModel.fillRolesAdapter(roles).subscribe(() -> txtRole.setAdapter(roles));
      d = viewModel.fillRanksAdapter(ranks).subscribe(() -> txtRank.setAdapter(ranks));
      if (unitId != -1) {
         d = Single.fromCallable(() -> viewModel.getUnit(unitId)).subscribe(unit -> {
            units.add(unit);
            spnUnit.setAdapter(units);
            spnUnit.setEnabled(false);
         });
      } else {
         d = viewModel.fillUnitsAdapter(units).subscribe(() -> spnUnit.setAdapter(units));
      }
   }

   public void btnAddClick(View view) {
      int mpId;
      try {
         mpId = Integer.parseUnsignedInt(txtMpId.getText().toString());
      } catch (NumberFormatException ex) {
         Toast.makeText(this, R.string.err_soldier_id_not_number, Toast.LENGTH_SHORT).show();
         return;
      }
      String name = txtName.getText().toString().trim();
      String role = txtRole.getText().toString().trim();
      String rank = txtRank.getText().toString().trim();
      Unit unit = (Unit)spnUnit.getSelectedItem();

      if (name.isEmpty()) {
         Toast.makeText(this, R.string.err_soldier_empty_name, Toast.LENGTH_SHORT).show();
         return;
      } else if (unit == null) {
         Toast.makeText(this, R.string.err_soldier_empty_unit, Toast.LENGTH_SHORT).show();
         return;
      }

      Disposable d = viewModel.addSoldier(mpId, unit, name, role, rank).subscribe(this::finish);
   }
}