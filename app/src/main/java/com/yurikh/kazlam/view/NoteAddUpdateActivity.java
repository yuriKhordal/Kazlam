package com.yurikh.kazlam.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Note;
import com.yurikh.kazlam.model.NoteTag;
import com.yurikh.kazlam.model.Soldier;
import com.yurikh.kazlam.viewmodel.NotesViewModel;

import java.util.Set;
import java.util.TreeSet;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;

public class NoteAddUpdateActivity extends AppCompatActivity {
   public static final String INTENT_SOLDIER_ID = "soldier_id";
   public static final String INTENT_UNIT_ID = "unit_id";
   public static final String INTENT_NOTE_ID = "note_id";
   public static final String INTENT_ADD_UPDATE = "is_add";

   NotesViewModel viewModel;
   boolean add;
   Note note;

   Spinner spnSoldiers;
   EditText txtTitle;
   EditText txtContent;
   AutoCompleteTextView txtTags;
   FlexboxLayout flxTags;
   Button btnAdd;
   Button btnUpdate;
   Button btnCopy;
   Button btnDelete;

   ArrayAdapter<NoteTag> dbTags;
   ArrayAdapter<Soldier> soldiers;
   Set<String> tags;

   public static Intent createIntentAdd(Context ctx) {
      return new Intent(ctx, NoteAddUpdateActivity.class)
         .putExtra(INTENT_SOLDIER_ID, -1)
         .putExtra(INTENT_UNIT_ID, -1)
         .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentAddFromUnit(Context ctx, long unitId) {
      return new Intent(ctx, NoteAddUpdateActivity.class)
         .putExtra(INTENT_SOLDIER_ID, -1)
         .putExtra(INTENT_UNIT_ID, unitId)
         .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentAddFromSoldier(Context ctx, long soldierId) {
      return new Intent(ctx, NoteAddUpdateActivity.class)
              .putExtra(INTENT_SOLDIER_ID, soldierId)
              .putExtra(INTENT_UNIT_ID, -1)
              .putExtra(INTENT_ADD_UPDATE, true);
   }

   public static Intent createIntentUpdate(Context ctx, long noteId) {
      return new Intent(ctx, NoteAddUpdateActivity.class)
         .putExtra(INTENT_NOTE_ID, noteId)
         .putExtra(INTENT_ADD_UPDATE, false);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
      setContentView(R.layout.activity_note_add_update);
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
         v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
         return insets;
      });

      setSupportActionBar(findViewById(R.id.toolbar));
      viewModel = new NotesViewModel(this);
      add = getIntent().getBooleanExtra(INTENT_ADD_UPDATE, true);

      spnSoldiers = findViewById(R.id.spn_soldiers);
      txtTitle = findViewById(R.id.txt_title);
      txtContent = findViewById(R.id.txt_content);
      txtTags = findViewById(R.id.txt_tags);
      flxTags = findViewById(R.id.flx_tags);
      btnAdd = findViewById(R.id.btn_add);
      btnUpdate = findViewById(R.id.btn_update);
      btnCopy = findViewById(R.id.btn_copy);
      btnDelete = findViewById(R.id.btn_delete);

      txtTags.addTextChangedListener(txtTagsTextChange);
      tags = new TreeSet<>();

      dbTags = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
      Disposable d = viewModel.fillTagsAdapter(dbTags).subscribe(() -> txtTags.setAdapter(dbTags));

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
      setTitle(R.string.note);
      btnAdd.setVisibility(View.GONE);

      long id = getIntent().getLongExtra(INTENT_NOTE_ID, -1);
      if (id == -1) {
         onCreateError();
         return;
      }
      Disposable d = viewModel.getNote(id)
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(n -> {
            note = n;
            txtTitle.setText(note.title);
            txtContent.setText(note.content);

            Disposable dd;
            dd = viewModel.getSoldier(note.soldierId).subscribe(soldier -> {
               soldiers.add(soldier);
               spnSoldiers.setAdapter(soldiers);
               spnSoldiers.setSelection(0);
               spnSoldiers.setEnabled(false);
            });
            dd = viewModel.getTags(id).subscribe(tags -> {
               tags.forEach(tag -> addTagSync(tag.name));
            });

         }, ex -> {throw ex;}, this::onCreateError);
   }

   private void onCreateError() {
      setTitle(R.string.err_note_not_exist);
      spnSoldiers.setEnabled(false);
      txtTitle.setEnabled(false);
      txtContent.setEnabled(false);
      txtTags.setEnabled(false);
      btnAdd.setEnabled(false);
      btnUpdate.setEnabled(false);
      btnCopy.setEnabled(false);
      btnDelete.setEnabled(false);

      Toast.makeText(this, R.string.err_note_not_exist, Toast.LENGTH_SHORT).show();
   }

   public boolean isAddMode() { return add; }
   public boolean isUpdateMode() { return !add; }

   Completable addTag(String tag) {
      return Completable.fromAction(() -> addTagSync(tag));
   }

   void addTagSync(String tag) {
      tag = tag.trim();
      if (tags.contains(tag) || tag.isEmpty()) return;

      tags.add(tag);
      View view = LayoutInflater.from(this)
         .inflate(R.layout.item_tag, flxTags, false);

      TextView lblTag = view.findViewById(R.id.lbl_tag);
      ImageView btnTagDelete = view.findViewById(R.id.btn_delete);

      lblTag.setText(tag);
      btnTagDelete.setTag(tag);
      btnTagDelete.setOnClickListener(this::btnTagDeleteClick);

      flxTags.addView(view);
   }

   // ============================== Events ==============================

   public void btnAddUpdateClick(View view) {
      Soldier soldier = (Soldier)spnSoldiers.getSelectedItem();
      String title = txtTitle.getText().toString().trim();
      String content = txtContent.getText().toString();
      addTagSync(txtTags.getText().toString());

      if (title.isEmpty()) {
         Toast.makeText(this, R.string.err_note_empty_title, Toast.LENGTH_SHORT).show();
         return;
      }

      if (isAddMode()) {
         Disposable d = viewModel.addNote(soldier, title, content, tags)
            .subscribe(this::finish);
      } else {
         Disposable d = viewModel.updateNote(note, title, content, tags).subscribe(() -> {
            finish();
            String toast = getString(R.string.update_success, title);
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
         });
      }
   }

   public void btnCopyClick(View view) {
      Soldier soldier = (Soldier)spnSoldiers.getSelectedItem();
      String title = txtTitle.getText().toString().trim();
      String content = txtContent.getText().toString();

      if (title.isEmpty()) {
         Toast.makeText(this, R.string.err_note_empty_title, Toast.LENGTH_SHORT).show();
         return;
      } else if (soldier == null) {
         Toast.makeText(this, R.string.err_note_empty_soldier, Toast.LENGTH_SHORT).show();
         return;
      }

      StringBuilder builder = new StringBuilder();
      builder.append(soldier.name).append('\n')
         .append(title);

      if (!content.isEmpty()) builder.append(":\n").append(content);
      builder.append('\n');

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
         .setMessage(getString(R.string.dialog_note_delete_msg, note.title))
         .setPositiveButton(R.string.dialog_yes, (a, b) -> {
            viewModel.deleteNote(note);
            finish();
         })
         .setNegativeButton(R.string.dialog_no, null);
      builder.create().show();
   }

   public void btnTagDeleteClick(View view) {
      String tag = (String)view.getTag();
      View parent = (View)view.getParent();

      tags.remove(tag);
      flxTags.removeView(parent);
   }

   TextWatcher txtTagsTextChange = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

      @Override
      public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

      @Override
      public void afterTextChanged(Editable editable) {
         String[] subs = editable.toString().split(" ");
         if (subs.length < 2) return;

         for (int i = 0; i < subs.length - 1; i++) {
            addTag(subs[i]).subscribe();
         }
         editable.clear();
         editable.append(subs[subs.length-1]);
      }
   };
}