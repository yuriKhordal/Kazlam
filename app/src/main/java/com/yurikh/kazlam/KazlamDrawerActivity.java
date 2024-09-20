package com.yurikh.kazlam;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.yurikh.kazlam.view.DisciplineListActivity;
import com.yurikh.kazlam.view.LeaveRequestListActivity;
import com.yurikh.kazlam.view.NoteListActivity;
import com.yurikh.kazlam.view.SoldierListActivity;
import com.yurikh.kazlam.view.UnitListActivity;
import com.yurikh.kazlam.view.UnitLogListActivity;

public class KazlamDrawerActivity extends AppCompatActivity {
   protected DrawerLayout drawerLayout;
   protected Toolbar toolbar;
   protected NavigationView navView;

   protected void loadViews() {
      drawerLayout = findViewById(R.id.lyt_drawer);
      toolbar = findViewById(R.id.toolbar);
      navView = findViewById(R.id.nav_view);

      setSupportActionBar(toolbar);
      toolbar.setNavigationOnClickListener(view -> drawerLayout.open());
      navView.setNavigationItemSelectedListener(this::navViewOnItemSelected);
   }

   protected boolean navViewOnItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();
      drawerLayout.close();
      if (id == R.id.menu_units) {
         startActivity(UnitListActivity.createIntent(this));
         return false;
      } else if (id == R.id.menu_soldiers) {
         startActivity(SoldierListActivity.createIntent(this));
         return false;
      } else if (id == R.id.menu_notes) {
         startActivity(NoteListActivity.createIntent(this));
         return false;
      } else if (id == R.id.menu_unit_logs) {
         startActivity(UnitLogListActivity.createIntent(this));
         return false;
      } else if (id == R.id.menu_leave_requests) {
         startActivity(LeaveRequestListActivity.createIntent(this));
         return false;
      } else if (id == R.id.menu_discipline) {
         startActivity(DisciplineListActivity.createIntent(this));
         return false;
      } /*else if (id == R.id.menu_settings) {
         return false;
      }*/
      return false;
   }
}
