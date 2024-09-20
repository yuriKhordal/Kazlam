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

import com.yurikh.kazlam.Helper;
import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.DisciplinaryNotice;
import com.yurikh.kazlam.viewmodel.DisciplineViewModel;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DisciplineNoticesFragment extends Fragment {
    private static final String ARG_UNIT_ID = "unitId";
    private static final String ARG_SOLDIER_ID = "soldierId";

    DisciplineViewModel viewModel;
    long unitId = -1;
    long soldierId = -1;

    Spinner spnSort;
    ImageView btnOrder;
    LinearLayout lytDisciplinaryNotices;
    ImageView btnAdd;

    List<DisciplineViewModel.DisciplinaryNoticeWrapper> disciplinaries;
    boolean sortAsc = true;

    public DisciplineNoticesFragment() {
        // Required empty public constructor
    }

    public static DisciplineNoticesFragment newInstanceByUnit(long unitId) {
        DisciplineNoticesFragment fragment = new DisciplineNoticesFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_UNIT_ID, unitId);
        args.putLong(ARG_SOLDIER_ID, -1);
        fragment.setArguments(args);
        return fragment;
    }

    public static DisciplineNoticesFragment newInstanceBySoldier(long soldierId) {
        DisciplineNoticesFragment fragment = new DisciplineNoticesFragment();
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

        viewModel = new DisciplineViewModel(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discipline_notices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lytDisciplinaryNotices = view.findViewById(R.id.lyt_disciplinary_notices);
        spnSort = view.findViewById(R.id.spn_sort_by);
        btnOrder = view.findViewById(R.id.btn_order);
        btnAdd = view.findViewById(R.id.btn_add);

        btnOrder.setOnClickListener(this::btnOrderClick);
        btnAdd.setOnClickListener(this::btnAddClick);

        spnSort.setSelection(DisciplineViewModel.SortBy.title.val);
        spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    private void reload() {
        lytDisciplinaryNotices.removeAllViews();
        int sort = spnSort.getSelectedItemPosition();

        Callable<List<DisciplineViewModel. DisciplinaryNoticeWrapper>> callable;
        if (unitId != -1) {
            callable = () -> viewModel.loadUnitNotices(unitId, sort, sortAsc);
        } else if (soldierId != -1) {
            callable = () -> viewModel.loadSoldierNotices(soldierId, sort, sortAsc);
        } else {
            return;
        }

        Disposable d = Single.fromCallable(callable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    disciplinaries = list;
                    disciplinaries.forEach(this::addNoticeRow);
                });
    }

    //Events:

    private void addNoticeRow(DisciplineViewModel.DisciplinaryNoticeWrapper wrapper) {
        View row = LayoutInflater.from(lytDisciplinaryNotices.getContext())
                .inflate(R.layout.item_disciplinary_notice, lytDisciplinaryNotices, false);

        TextView lblSoldier = row.findViewById(R.id.lbl_soldier);
        TextView lblTitle = row.findViewById(R.id.lbl_title);
        View dividerDescription = row.findViewById(R.id.divider_description);
        TextView lblDescription = row.findViewById(R.id.lbl_description);
        View dividerPunishment = row.findViewById(R.id.divider_punishment);
        TextView lblPunishment = row.findViewById(R.id.lbl_punishment);
        TextView lblDate = row.findViewById(R.id.lbl_date);
        ImageView btnDelete = row.findViewById(R.id.btn_delete);

        lblSoldier.setText(wrapper.soldier.name);
        lblTitle.setText(wrapper.notice.title);
        lblDate.setText(wrapper.notice.date);

        if (Helper.StringEmpty(wrapper.notice.description)) {
            dividerDescription.setVisibility(View.GONE);
            lblDescription.setVisibility(View.GONE);
        } else {
            lblDescription.setText(wrapper.notice.description);
        }
        if (Helper.StringEmpty(wrapper.notice.punishment)) {
            dividerPunishment.setVisibility(View.GONE);
            lblPunishment.setVisibility(View.GONE);
        } else {
            lblPunishment.setText(wrapper.notice.punishment);
        }

        row.setTag(wrapper.notice);
        btnDelete.setTag(wrapper);

        row.setOnClickListener(this::noticeClick);
        btnDelete.setOnClickListener(this::btnDeleteClick);

        lytDisciplinaryNotices.addView(row);
    }

    public void btnAddClick(View view) {
        if (unitId != -1) {
            startActivity(DisciplineAddUpdateActivity.createIntentAddFromUnit(getContext(), unitId));
        } else if (soldierId != -1) {
            startActivity(DisciplineAddUpdateActivity.createIntentAddFromSoldier(getContext(), soldierId));
        }
    }

    private void btnDeleteClick(View view) {
        DisciplineViewModel.DisciplinaryNoticeWrapper wrapper =
                (DisciplineViewModel.DisciplinaryNoticeWrapper)view.getTag();
        int pos = disciplinaries.indexOf(wrapper);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_are_you_sure)
                .setMessage(getString(R.string.dialog_discipline_delete_msg, wrapper.notice.title))
                .setPositiveButton(R.string.dialog_yes, (a, b) -> {
                    disciplinaries.remove(pos);
                    lytDisciplinaryNotices.removeViewAt(pos);
                    viewModel.deleteNotice(wrapper.notice);
                })
                .setNegativeButton(R.string.dialog_no, null);
        builder.create().show();
    }

    private void noticeClick(View view) {
        DisciplinaryNotice notice = (DisciplinaryNotice)view.getTag();
        startActivity(DisciplineAddUpdateActivity.createIntentUpdate(getContext(), notice.id));
    }

    public void btnOrderClick(View view) {
        sortAsc = !sortAsc;

        if (sortAsc) btnOrder.setImageResource(R.drawable.baseline_arrow_downward_24);
        else btnOrder.setImageResource(R.drawable.baseline_arrow_upward_24);

        int position = spnSort.getSelectedItemPosition();
        viewModel.sortBy(disciplinaries, position, sortAsc);
        lytDisciplinaryNotices.removeAllViews();
        disciplinaries.forEach(this::addNoticeRow);
    }

    AdapterView.OnItemSelectedListener spnSortItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (disciplinaries == null) return;

                    viewModel.sortBy(disciplinaries, position, sortAsc);
                    lytDisciplinaryNotices.removeAllViews();
                    disciplinaries.forEach(n -> addNoticeRow(n));
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            };
}