package com.yurikh.kazlam.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yurikh.kazlam.Helper;
import com.yurikh.kazlam.KazlamDrawerActivity;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.viewmodel.UnitsViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UnitActivity extends KazlamDrawerActivity {
   private static final String INTENT_UNIT_ID = "unit_id";

   UnitsViewModel viewModel;

   long id;
   Unit unit;
   Unit mother;
   Soldier commander;

   EditText txtName;
   Spinner spnMother;
   Spinner spnCommander;
   Button btnUpdate;

   TabLayout tabs;
   ViewPager2 viewPager;

   ArrayAdapter<Unit> mothersAdapter;
   ArrayAdapter<Soldier> commandersAdapter;

   /**
    * Create an intent leading to this activity.
    * @param ctx The context of the intent.
    * @param unitId The id of the unit to view/edit.
    */
   public static Intent createIntent(Context ctx, long unitId) {
      return new Intent(ctx, UnitActivity.class)
         .putExtra(INTENT_UNIT_ID, unitId);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_unit);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      super.loadViews();
      viewModel = new UnitsViewModel(this);
      id = getIntent().getLongExtra(INTENT_UNIT_ID, -1);
      txtName = findViewById(R.id.txt_name);
      spnMother = findViewById(R.id.spn_mother);
      spnCommander = findViewById(R.id.spn_commander);
      btnUpdate = findViewById(R.id.btn_update);

      tabs = findViewById(R.id.tabs);
      viewPager = findViewById(R.id.view_pager);
   }

   @Override
   protected void onResume() {
      super.onResume();
      reload();
   }

   public void reload() {
      mothersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
      commandersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
      Disposable d = Single.fromCallable(
         () -> viewModel.initialize(id, mothersAdapter, commandersAdapter))
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(triplet -> {
            unit = triplet.getFirst();
            mother = triplet.getSecond();
            commander = triplet.getThird();
            setViews();
         });
   }

   private void setViews() {
      if (unit == null) {
         setTitle(R.string.err_unit_not_exist);
         txtName.setEnabled(false);
         spnMother.setEnabled(false);
         spnCommander.setEnabled(false);
         btnUpdate.setEnabled(false);
         tabs.setEnabled(false);
         viewPager.setEnabled(false);
      } else {
         setTitle(unit.name);
         txtName.setText(unit.name);
         spnMother.setAdapter(mothersAdapter);
         spnCommander.setAdapter(commandersAdapter);
         if (mother != null)
            spnMother.setSelection(mothersAdapter.getPosition(mother));
         if (commander != null)
            spnCommander.setSelection(commandersAdapter.getPosition(commander));

         viewPager.setAdapter(new PagerAdapter(this));
         // Disable swiping because table is usually too wide
//         viewPager.setUserInputEnabled(false);

         int[] tabNames = {
                 R.string.soldiers_title, R.string.notes_title, R.string.unit_logs_title, R.string.disc_notices_title,
                 R.string.leave_requests_title, R.string.units_title
         };
         new TabLayoutMediator(tabs, viewPager, (tab, position) ->
                 tab.setText(tabNames[position])).attach();
      }
   }

   // ============================== Events ==============================

   public void btnUpdateClick(View view) {
      String name = txtName.getText().toString().trim();
      if (Helper.StringEmpty(name)) {
         Toast.makeText(this, R.string.err_unit_empty_name, Toast.LENGTH_LONG).show();
         return;
      }
      if (spnMother.getSelectedItemPosition() == 0)
         mother = null;
      else mother = (Unit)spnMother.getSelectedItem();
      if (spnCommander.getSelectedItemPosition() == 0)
         commander = null;
      else commander = (Soldier)spnCommander.getSelectedItem();

      setTitle(name);
      Disposable d =viewModel.updateUnit(unit, name, mother, commander).subscribe(() -> {
         String toast = getString(R.string.update_success, name);
         Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
         finish();
      });
   }

   private class PagerAdapter extends FragmentStateAdapter {
      public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
         super(fragmentActivity);
      }

      @NonNull
      @Override
      public Fragment createFragment(int position) {
         final int tab_soldiers = 0;
         final int tab_notes = 1;
         final int tab_unit_logs = 2;
         final int tab_discipline = 3;
         final int tab_leave = 4;
         final int tab_units = 5;

         switch(position) {
            case tab_soldiers: return UnitSoldiersFragment.newInstance(id);
            case tab_notes: return NotesFragment.newInstanceByUnit(id);
            case tab_unit_logs: return UnitLogsFragment.newInstance(id);
            case tab_discipline: return DisciplineNoticesFragment.newInstanceByUnit(id);
            case tab_leave: return LeaveRequestsFragment.newInstanceByUnit(id);
            case tab_units: return UnitsFragment.newInstance(id);
            default: return null;
         }
      }

      @Override
      public int getItemCount() {
         return 6;
      }
   }
}