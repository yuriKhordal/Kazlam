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

import com.yurikh.kazlam.KazlamDrawerActivity;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Note;
import com.yurikh.kazlam.viewmodel.NotesViewModel;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NoteListActivity extends KazlamDrawerActivity {
   NotesViewModel viewModel;

   Spinner spnSort;
   ImageView btnOrder;
LinearLayout lytNotes;
//   HorizontalScrollView hscl;

   List<NotesViewModel.NoteWrapper> notes;
   boolean sortAsc = true;

   public static Intent createIntent(Context ctx) {
      return new Intent(ctx, NoteListActivity.class);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_note_list);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      super.loadViews();
      viewModel = new NotesViewModel(this);
      setTitle(R.string.notes_title);
      lytNotes = findViewById(R.id.lyt_notes);
      spnSort = findViewById(R.id.spn_sort_by);
      btnOrder = findViewById(R.id.btn_order);

      spnSort.setSelection(NotesViewModel.SortBy.title.val);
      spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);

//      hscl = (HorizontalScrollView) tblNotes.getParent().getParent();
   }

   @Override
   protected void onResume() {
      super.onResume();

      reload();
   }

   private void reload() {
      lytNotes.removeAllViews();
//      tblNotes.addView(rowHeader);

      int sort = spnSort.getSelectedItemPosition();
      Disposable d = Single.fromCallable(
            () -> viewModel.loadNotes(sort, sortAsc))
         .subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(list -> {
            notes = list;
            notes.forEach(this::addNoteRow);
         });

//      hscl.post(() -> hscl.smoothScrollTo(hscl.getWidth(), 0));
   }

   //Events:

   private void addNoteRow(NotesViewModel.NoteWrapper wrapper) {
      View row = LayoutInflater.from(lytNotes.getContext())
         .inflate(R.layout.item_note, lytNotes, false);

//      TextView lblNum = row.findViewById(R.id.lbl_lineNum);
      TextView lblSoldier = row.findViewById(R.id.lbl_soldier);
      TextView lblTitle = row.findViewById(R.id.lbl_title);
      TextView lblContent = row.findViewById(R.id.lbl_content);
      TextView lblUpdateDate = row.findViewById(R.id.lbl_update_date);
      View tagsDivider = row.findViewById(R.id.divider_tags);
      TextView lblTags = row.findViewById(R.id.lbl_tags);
      ImageView btnDelete = row.findViewById(R.id.btn_delete);

//      lblNum.setText("" + notes.indexOf(note));
      lblSoldier.setText(wrapper.soldier.name);
      lblTitle.setText(wrapper.note.title);
      lblContent.setText(wrapper.note.content);
      lblUpdateDate.setText(wrapper.note.modifyDate);

      if (wrapper.tags.isEmpty()) {
         lblTags.setVisibility(View.GONE);
         tagsDivider.setVisibility(View.GONE);
      } else {
         lblTags.setText(wrapper.tags.stream().map(tag -> tag.name)
                 .collect(Collectors.joining(", ")));
      }


      row.setTag(wrapper.note);
      btnDelete.setTag(wrapper);

      row.setOnClickListener(this::noteClick);
      btnDelete.setOnClickListener(this::btnDeleteClick);

      lytNotes.addView(row);
   }

   public void btnAddClick(View view) {
      startActivity(NoteAddUpdateActivity.createIntentAdd(this));
   }

   private void btnDeleteClick(View view) {
      NotesViewModel.NoteWrapper wrapper = (NotesViewModel.NoteWrapper)view.getTag();
      int pos = notes.indexOf(wrapper);

      AlertDialog.Builder builder = new AlertDialog.Builder(this)
         .setTitle(R.string.dialog_are_you_sure)
         .setMessage(getString(R.string.dialog_note_delete_msg, wrapper.note.title))
         .setPositiveButton(R.string.dialog_yes, (a, b) -> {
            notes.remove(pos);
            lytNotes.removeViewAt(pos);
            viewModel.deleteNote(wrapper.note);
         })
         .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }

   private void noteClick(View view) {
      Note note = (Note)view.getTag();
      startActivity(NoteAddUpdateActivity.createIntentUpdate(this, note.id));
   }

   public void btnOrderClick(View view) {
      sortAsc = !sortAsc;

      if (sortAsc) btnOrder.setImageResource(R.drawable.baseline_arrow_downward_24);
      else btnOrder.setImageResource(R.drawable.baseline_arrow_upward_24);

      int position = spnSort.getSelectedItemPosition();
      viewModel.sortBy(notes, position, sortAsc);
      lytNotes.removeAllViews();
      notes.forEach(this::addNoteRow);
   }

   AdapterView.OnItemSelectedListener spnSortItemSelectedListener =
      new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (notes == null) return;

            viewModel.sortBy(notes, position, sortAsc);
            lytNotes.removeAllViews();
            notes.forEach(n -> addNoteRow(n));
         }
         @Override
         public void onNothingSelected(AdapterView<?> parent) {}
      };

   @Override
   protected boolean navViewOnItemSelected(@NonNull MenuItem item) {
      // Don't actually switch activities when choosing the same activity.
      if (item.getItemId() == R.id.menu_notes) {
         drawerLayout.close();
         reload();
         return false;
      }
      return super.navViewOnItemSelected(item);
   }
}