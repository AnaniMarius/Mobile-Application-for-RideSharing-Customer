package ro.ananimarius.allridev3customer.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.math.RoundingMode;
import java.util.List;

import ro.ananimarius.allridev3customer.Common.DriverDTO;
import ro.ananimarius.allridev3customer.R;
import ro.ananimarius.allridev3customer.ui.home.DriverViewHolder;

public class DriverListAdapter extends RecyclerView.Adapter<DriverViewHolder> {
    private List<DriverDTO> drivers;
    private OnDriverClickListener onDriverClickListener;

    public interface OnDriverClickListener {
        void onDriverClick(int position);
    }

    public DriverListAdapter(List<DriverDTO> drivers, OnDriverClickListener onDriverClickListener) {
        this.drivers = drivers;
        this.onDriverClickListener = onDriverClickListener;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_instance, parent, false);
        return new DriverViewHolder(view, onDriverClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        DriverDTO driver = drivers.get(position);
        holder.nameTextView.setText("Driver's Name: "+driver.getFirstName());
        holder.surnameTextView.setText("Driver's Surname: "+driver.getLastName());
        holder.ratingTextView.setText("Price for ride: "+driver.getCurrentRating());
        holder.distanceTextView.setText("Distance to arrive: "+String.format("%.2f",driver.getCurrentRideTotalDistance()));
        holder.timeTextView.setText("Time to arrive: "+(driver.getCurrentRideTotalTime()).setScale(2, RoundingMode.HALF_UP));
        holder.priceTextView.setText("Price for ride: "+(driver.getCurrentRidePrice()).setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    public void clearDrivers() {
        drivers.clear();
        notifyDataSetChanged();
    }
}
