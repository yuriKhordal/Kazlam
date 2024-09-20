package com.yurikh.kazlam.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.kazlam.Helper;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.viewmodel.UnitsViewModel;

import io.reactivex.rxjava3.disposables.Disposable;

public class UnitAddActivity extends AppCompatActivity {
   EditText txtName;
   Spinner spnMothers;
   UnitsViewModel viewModel;

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, UnitAddActivity.class);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_unit_add);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      viewModel = new UnitsViewModel(this);
      setTitle(R.string.unit_add);

      txtName = findViewById(R.id.txt_name);
      spnMothers = findViewById(R.id.spn_mother);

      int layout = android.R.layout.simple_spinner_dropdown_item;
      Disposable d = viewModel.getUnitsAdapter(layout)
         .subscribe(adapter -> spnMothers.setAdapter(adapter));
   }

   public void btnAddClick(View view) {
      Unit mother = (Unit)spnMothers.getSelectedItem();
      String name = txtName.getText().toString().trim();
      if (Helper.StringEmpty(name)) {
         Toast.makeText(this, R.string.err_unit_empty_name, Toast.LENGTH_LONG).show();
         return;
      }

      viewModel.addUnit(name, mother);
      finish();
   }
}