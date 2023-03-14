package ro.ananimarius.allridev3customer.ui.home;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ro.ananimarius.allridev3customer.R;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private View bottomSheet;

    public static MyBottomSheetDialogFragment newInstance(View bottomSheet, DriverListAdapter driverAdapter) {
        MyBottomSheetDialogFragment fragment = new MyBottomSheetDialogFragment(driverAdapter);
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

    //clear the drivers after the sliding panel is dismissed
    private DriverListAdapter driverAdapter;
    public MyBottomSheetDialogFragment(DriverListAdapter driverAdapter) {
        this.driverAdapter = driverAdapter;
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        driverAdapter.clearDrivers();
    }
}