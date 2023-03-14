package ro.ananimarius.allridev3customer.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ro.ananimarius.allridev3customer.R;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private View bottomSheet;

    public static MyBottomSheetDialogFragment newInstance(View bottomSheet) {
        MyBottomSheetDialogFragment fragment = new MyBottomSheetDialogFragment();
        fragment.setBottomSheet(bottomSheet);
        return fragment;
    }

    public void setBottomSheet(View bottomSheet) {
        this.bottomSheet = bottomSheet;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return bottomSheet;
    }
}


/*
package ro.ananimarius.allridev3customer.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

import ro.ananimarius.allridev3customer.R;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private List<Driver> drivers;

    public static MyBottomSheetDialogFragment newInstance(List<Driver> drivers) {
        MyBottomSheetDialogFragment fragment = new MyBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("drivers", new ArrayList<>(drivers));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_bottom_sheet, container, false);
        RecyclerView driverList = view.findViewById(R.id.driver_list);
        driverList.setLayoutManager(new LinearLayoutManager(requireContext()));
        drivers = getArguments().getParcelableArrayList("drivers");
        DriverListAdapter driverAdapter = new DriverListAdapter(drivers);
        driverList.setAdapter(driverAdapter);
        return view;
    }
}

 */