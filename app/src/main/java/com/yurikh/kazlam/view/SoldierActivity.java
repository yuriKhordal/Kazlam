package com.yurikh.kazlam.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.yurikh.kazlam.viewmodel.SoldiersViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SoldierActivity extends KazlamDrawerActivity {
   private static final String INTENT_SOLDIER_ID = "soldier_id";

   SoldiersViewModel viewModel;
   Soldier soldier;

   EditText txtMpId;
   EditText txtName;
   AutoCompleteTextView txtRole;
   AutoCompleteTextView txtRank;
   Spinner spnUnit;
   Button btnUpdate;
   Button btnCopy;
   Button btnDelete;

   TabLayout tabs;
   ViewPager2 viewPager;

   ArrayAdapter<String> roles;
   ArrayAdapter<String> ranks;
   ArrayAdapter<Unit> units;

   /**
    * Create an intent leading to this activity.
    * @param ctx The context of the intent.
    * @param soldierId The id of the unit to view/edit.
    */
   public static Intent createIntent(Context ctx, long soldierId) {
      return new Intent(ctx, SoldierActivity.class)
         .putExtra(INTENT_SOLDIER_ID, soldierId);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_soldier);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      loadViews();
      viewModel = new SoldiersViewModel(this);

      txtMpId = findViewById(R.id.txt_mp_id);
      txtName = findViewById(R.id.txt_name);
      txtRole = findViewById(R.id.txt_role);
      txtRank = findViewById(R.id.txt_rank);
      spnUnit = findViewById(R.id.spn_unit);
      btnUpdate = findViewById(R.id.btn_update);
      btnCopy = findViewById(R.id.btn_copy);
      btnDelete = findViewById(R.id.btn_delete);

      tabs = findViewById(R.id.tabs);
      viewPager = findViewById(R.id.view_pager);
   }

   @Override
   protected void onResume() {
      super.onResume();
      reload();
   }

   private void reload() {
      long id = getIntent().getLongExtra(INTENT_SOLDIER_ID, -1);

      roles = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
      ranks = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
      units = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);

      Disposable d;
      d = viewModel.fillRolesAdapter(roles).subscribe(() -> txtRole.setAdapter(roles));
      d = viewModel.fillRanksAdapter(ranks).subscribe(() -> txtRank.setAdapter(ranks));
      d = viewModel.fillUnitsAdapter(units).subscribe(() -> {
         spnUnit.setAdapter(units);
         Disposable dd = Single.fromCallable(() -> viewModel.getSoldier(id))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(result -> {
               soldier = result;
               setViews();
            });
      });

   }

   void setViews() {
      if (soldier == null) {
         setTitle(R.string.err_soldier_not_exist);
         txtMpId.setEnabled(false);
         txtName.setEnabled(false);
         txtRole.setEnabled(false);
         txtRank.setEnabled(false);
         spnUnit.setEnabled(false);
         btnUpdate.setEnabled(false);
         btnCopy.setEnabled(false);
         btnDelete.setEnabled(false);
         tabs.setEnabled(false);
         viewPager.setEnabled(false);
         return;
      }

      setTitle(soldier.name);
      txtMpId.setText(soldier.mpId + "");
      txtName.setText(soldier.name);
      txtRole.setText(soldier.role);
      txtRank.setText(soldier.rank);

      int pos = Helper.searchByKey(units, soldier.unitId, obj -> ((Unit)obj).id);
      spnUnit.setSelection(pos);

      viewPager.setAdapter(new PagerAdapter(this));
      // Disable swiping because table is usually too wide
//         viewPager.setUserInputEnabled(false);

      int[] tabNames = {
              R.string.notes_title, R.string.disc_notices_title, R.string.leave_requests_title
      };
      new TabLayoutMediator(tabs, viewPager, (tab, i) -> tab.setText(tabNames[i])).attach();
   }

   // ============================== Events ==============================

   public void btnUpdateClick(View view) {
      int mpId;
      try {
         mpId = Integer.parseUnsignedInt(txtMpId.getText().toString());
      } catch (NumberFormatException ex) {
         Toast.makeText(this, R.string.err_soldier_id_not_number, Toast.LENGTH_SHORT).show();
         return;
      }
      String name = txtName.getText().toString();
      String role = txtRole.getText().toString();
      String rank = txtRank.getText().toString();
      Unit unit = (Unit)spnUnit.getSelectedItem();

      if (name.isEmpty()) {
         Toast.makeText(this, R.string.err_soldier_empty_name, Toast.LENGTH_SHORT).show();
         return;
      }

      Soldier updated = new Soldier(mpId, unit.id, name, rank, role);
      Disposable d = viewModel.updateSoldier(soldier, updated).subscribe(() -> {
         finish();
         String toast = getString(R.string.update_success, name);
         Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
      });
   }

   public void btnCopyClick(View view) {
      Unit unit = (Unit)spnUnit.getSelectedItem();
      String mpId = txtMpId.getText().toString().trim();
      String name = txtName.getText().toString().trim();
      String role = txtRole.getText().toString().trim();
      String rank = txtRank.getText().toString().trim();

      if (name.isEmpty()) {
         Toast.makeText(this, R.string.err_soldier_empty_name, Toast.LENGTH_SHORT).show();
         return;
      }

      StringBuilder builder = new StringBuilder();
      builder.append(name);

      if (!mpId.isEmpty())
         builder.append('(').append(mpId).append(')');
      builder.append(":\n").append(unit.name).append('\n');
      if (!rank.isEmpty())
         builder.append(getText(R.string.soldier_rank)).append(": ").append(rank).append('\n');
      if (!role.isEmpty())
         builder.append(getText(R.string.soldier_role)).append(": ").append(role).append('\n');

      ClipboardManager manager =
         (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData data = ClipData.newPlainText(name, builder.toString());
      manager.setPrimaryClip(data);

      String toast = getString(R.string.copy_success, name);
      Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
   }

   public void btnDeleteClick(View view) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this)
         .setTitle(R.string.dialog_are_you_sure)
         .setMessage(getString(R.string.dialog_soldier_delete_msg, soldier.name))
         .setPositiveButton(R.string.dialog_yes, (a, b) -> {
            viewModel.deleteSoldier(soldier).subscribe(this::finish);
         })
         .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }

   private class PagerAdapter extends FragmentStateAdapter {
      public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
         super(fragmentActivity);
      }

      @NonNull
      @Override
      public Fragment createFragment(int position) {
         final int tab_notes = 0;
         final int tab_discipline = 1;
         final int tab_leave = 2;

         switch(position) {
            case tab_notes: return NotesFragment.newInstanceBySoldier(soldier.id);
            case tab_discipline: return DisciplineNoticesFragment.newInstanceBySoldier(soldier.id);
            case tab_leave: return LeaveRequestsFragment.newInstanceBySoldier(soldier.id);
            default: return null;
         }
      }

      @Override
      public int getItemCount() {
         return 3;
      }
   }
}