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
import com.yurikh.kazlam.model.LeaveRequest;
import com.yurikh.kazlam.viewmodel.DisciplineViewModel;
import com.yurikh.kazlam.viewmodel.LeaveRequestViewModel;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LeaveRequestsFragment extends Fragment {
    private static final String ARG_UNIT_ID = "unitId";
    private static final String ARG_SOLDIER_ID = "soldierId";

    LeaveRequestViewModel viewModel;
    long unitId = -1;
    long soldierId = -1;

    Spinner spnSort;
    ImageView btnOrder;
    LinearLayout lytLeaveRequests;
    ImageView btnAdd;

    List<LeaveRequestViewModel.LeaveRequestWrapper> leaveRequests;
    boolean sortAsc = true;

    public LeaveRequestsFragment() {
        // Required empty public constructor
    }

    public static LeaveRequestsFragment newInstanceByUnit(long unitId) {
        LeaveRequestsFragment fragment = new LeaveRequestsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_UNIT_ID, unitId);
        args.putLong(ARG_SOLDIER_ID, -1);
        fragment.setArguments(args);
        return fragment;
    }

    public static LeaveRequestsFragment newInstanceBySoldier(long soldierId) {
        LeaveRequestsFragment fragment = new LeaveRequestsFragment();
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
        viewModel = new LeaveRequestViewModel(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leave_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lytLeaveRequests = view.findViewById(R.id.lyt_leave_requests);
        spnSort = view.findViewById(R.id.spn_sort_by);
        btnOrder = view.findViewById(R.id.btn_order);
        btnAdd = view.findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(this::btnAddClick);
        btnOrder.setOnClickListener(this::btnOrderClick);

        spnSort.setSelection(DisciplineViewModel.SortBy.title.val);
        spnSort.setOnItemSelectedListener(spnSortItemSelectedListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    private void reload() {
        lytLeaveRequests.removeAllViews();

        int sort = spnSort.getSelectedItemPosition();
        Callable<List<LeaveRequestViewModel. LeaveRequestWrapper>> callable;
        if (unitId != -1) {
            callable = () -> viewModel.loadLeaveRequestsByUnit(unitId, sort, sortAsc);
        } else if (soldierId != -1) {
            callable = () -> viewModel.loadLeaveRequestsBySoldier(soldierId, sort, sortAsc);
        } else {
            return;
        }

        Disposable d = Single.fromCallable(callable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    leaveRequests = list;
                    leaveRequests.forEach(this::addLeaveRequestRow);
                });
    }

    //Events:

    private void addLeaveRequestRow(LeaveRequestViewModel.LeaveRequestWrapper wrap) {
        View row = LayoutInflater.from(lytLeaveRequests.getContext())
                .inflate(R.layout.item_leave_request, lytLeaveRequests, false);

        TextView lblDate = row.findViewById(R.id.txt_date);
        TextView lblReturnDate = row.findViewById(R.id.txt_return_date);
        TextView lblStatus = row.findViewById(R.id.txt_status);
        TextView lblReason = row.findViewById(R.id.lbl_reason);
        TextView lblSoldier = row.findViewById(R.id.lbl_soldier);
        ImageView btnDelete = row.findViewById(R.id.btn_delete);

        lblDate.setText(wrap.request.date);
        lblReturnDate.setText(wrap.request.returnDate);
        lblStatus.setText(wrap.request.status);
        lblReason.setText(wrap.request.reason);
        lblSoldier.setText(wrap.soldier.name);

        row.setTag(wrap.request);
        btnDelete.setTag(wrap);

        row.setOnClickListener(this::noticeClick);
        btnDelete.setOnClickListener(this::btnDeleteClick);

        lytLeaveRequests.addView(row);
    }

    public void btnAddClick(View view) {
        if (unitId != -1) {
            startActivity(LeaveRequestAddUpdateActivity.createIntentAddFromUnit(getContext(), unitId));
        } else if (soldierId != -1) {
            startActivity(LeaveRequestAddUpdateActivity.createIntentAddFromSoldier(getContext(), soldierId));
        }
    }

    private void btnDeleteClick(View view) {
        LeaveRequestViewModel.LeaveRequestWrapper wrap =
                (LeaveRequestViewModel.LeaveRequestWrapper)view.getTag();
        int pos = leaveRequests.indexOf(wrap);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_are_you_sure)
                .setMessage(getString(R.string.dialog_leave_request_delete_msg, wrap.request.reason))
                .setPositiveButton(R.string.dialog_yes, (a, b) -> {
                    leaveRequests.remove(pos);
                    lytLeaveRequests.removeViewAt(pos);
                    viewModel.deleteNotice(wrap.request);
                })
                .setNegativeButton(R.string.dialog_no, null);
        builder.create().show();
    }

    private void noticeClick(View view) {
        LeaveRequest request = (LeaveRequest)view.getTag();
        startActivity(LeaveRequestAddUpdateActivity.createIntentUpdate(getContext(), request.id));
    }

    public void btnOrderClick(View view) {
        sortAsc = !sortAsc;

        if (sortAsc) btnOrder.setImageResource(R.drawable.baseline_arrow_downward_24);
        else btnOrder.setImageResource(R.drawable.baseline_arrow_upward_24);

        int position = spnSort.getSelectedItemPosition();
        viewModel.sortBy(leaveRequests, position, sortAsc);
        lytLeaveRequests.removeAllViews();
        leaveRequests.forEach(this::addLeaveRequestRow);
    }

    AdapterView.OnItemSelectedListener spnSortItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (leaveRequests == null) return;

                    viewModel.sortBy(leaveRequests, position, sortAsc);
                    lytLeaveRequests.removeAllViews();
                    leaveRequests.forEach(n -> addLeaveRequestRow(n));
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            };
}