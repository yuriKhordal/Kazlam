package com.yurikh.kazlam.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yurikh.kazlam.R;
import com.yurikh.kazlam.model.Unit;
import com.yurikh.kazlam.viewmodel.UnitsViewModel;

import java.util.List;

public class UnitsFragment extends Fragment {
    private static final String ARG_UNIT_ID = "unit_id";

    UnitsViewModel viewModel;
    long unitId = -1;

    LinearLayout lytUnits;
    ImageView btnAdd;

    List<Unit> units;

    public UnitsFragment() {
        // Required empty public constructor
    }

    public static UnitsFragment newInstance(long unitId) {
        UnitsFragment fragment = new UnitsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_UNIT_ID, unitId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            unitId = getArguments().getLong(ARG_UNIT_ID, -1);
        }
        viewModel = new UnitsViewModel(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_units, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lytUnits = view.findViewById(R.id.lyt_units);
        btnAdd = view.findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(this::btnAddClick);
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    private void reload() {
        lytUnits.removeAllViews();
        viewModel.forEachSubUnitLevel(unitId, this::loadUnitViews);
    }

    public void loadUnitViews(Unit unit, int level) {
        View view = LayoutInflater.from(lytUnits.getContext())
                .inflate(R.layout.item_unit, lytUnits, false);
        view.setTag(unit);
        view.setOnClickListener(this::unitViewOnClick);

        TextView lblName = view.findViewById(R.id.lbl_name);
        String indentedName = "\t\t".repeat(level) + unit.name;
        lblName.setText(indentedName);

        View btnDelete = view.findViewById(R.id.btn_delete);
        btnDelete.setTag(unit);
        btnDelete.setOnClickListener(this::btnDeleteOnClick);

        lytUnits.addView(view);
    }

    // Events:
    private void btnAddClick(View view) {
        startActivity(UnitAddActivity.createIntent(getContext()));
    }

    private void unitViewOnClick(View view) {
        Unit unit = (Unit)view.getTag();
        startActivity(UnitActivity.createIntent(getContext(), unit.id));
    }

    private void btnDeleteOnClick(View view) {
        Unit unit = (Unit)view.getTag();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_are_you_sure)
                .setMessage(getString(R.string.dialog_unit_delete_msg, unit.name))
                .setPositiveButton(R.string.dialog_yes, (a, b) -> viewModel.deleteUnit(unit))
                .setNegativeButton(R.string.dialog_no, null)
                .setOnDismissListener(d -> reload());
        builder.create().show();
    }
}