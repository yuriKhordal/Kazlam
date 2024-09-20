package com.yurikh.kazlam.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Note;
import com.yurikh.kazlam.viewmodel.NotesViewModel;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotesFragment extends Fragment {
    private static final String ARG_UNIT_ID = "unitId";
    private static final String ARG_SOLDIER_ID = "soldierId";

    NotesViewModel viewModel;
    long unitId = -1;
    long soldierId = -1;

    Spinner spnSort;
    ImageView btnOrder;
    LinearLayout lytNotes;
    ImageView btnAdd;

    List<NotesViewModel.NoteWrapper> notes;
    boolean sortAsc = true;

    public NotesFragment() {
        // Required empty public constructor
    }

    public static NotesFragment newInstanceByUnit(long unitId) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_UNIT_ID, unitId);
        args.putLong(ARG_SOLDIER_ID, -1);
        fragment.setArguments(args);
        return fragment;
    }

    public static NotesFragment newInstanceBySoldier(long soldierId) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_UNIT_ID, -1);
        args.putLong(ARG_SOLDIER_ID, soldierId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            unitId = getArguments().getLong(ARG_UNIT_ID, -1);
            soldierId = getArguments().getLong(ARG_SOLDIER_ID, -1);
        }
        viewModel = new NotesViewModel(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lytNotes = view.findViewById(R.id.lyt_notes);
        spnSort = view.findViewById(R.id.spn_sort_by);
        btnOrder = view.findViewById(R.id.btn_order);
        btnAdd = view.findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(this::btnAddClick);
        btnOrder.setOnClickListener(this::btnOrderClick);

        spnSort.setSelection(NotesViewModel.SortBy.title.val);
        spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    private void reload() {
        lytNotes.removeAllViews();
        int sort = spnSort.getSelectedItemPosition();

        Callable<List<NotesViewModel. NoteWrapper>> callable;
        if (unitId != -1) {
            callable = () -> viewModel.loadUnitNotes(unitId, sort, sortAsc);
        } else if (soldierId != -1) {
            callable = () -> viewModel.loadSoldierNotes(soldierId, sort, sortAsc);
        } else {
            return;
        }

        Disposable d = Single.fromCallable(callable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    notes = list;
                    notes.forEach(this::addNoteRow);
                });
    }

    //Events:

    private void addNoteRow(NotesViewModel.NoteWrapper wrapper) {
        View row = LayoutInflater.from(lytNotes.getContext())
                .inflate(R.layout.item_note, lytNotes, false);

        TextView lblSoldier = row.findViewById(R.id.lbl_soldier);
        TextView lblTitle = row.findViewById(R.id.lbl_title);
        TextView lblContent = row.findViewById(R.id.lbl_content);
        TextView lblUpdateDate = row.findViewById(R.id.lbl_update_date);
        View tagsDivider = row.findViewById(R.id.divider_tags);
        TextView lblTags = row.findViewById(R.id.lbl_tags);
        ImageView btnDelete = row.findViewById(R.id.btn_delete);

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
        if (unitId != -1) {
            startActivity(NoteAddUpdateActivity.createIntentAddFromUnit(getContext(), unitId));
        } else if (soldierId != -1) {
            startActivity(NoteAddUpdateActivity.createIntentAddFromSoldier(getContext(), soldierId));
        }
    }

    private void btnDeleteClick(View view) {
        NotesViewModel.NoteWrapper wrapper = (NotesViewModel.NoteWrapper)view.getTag();
        int pos = notes.indexOf(wrapper);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
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
        startActivity(NoteAddUpdateActivity.createIntentUpdate(getContext(), note.id));
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
}