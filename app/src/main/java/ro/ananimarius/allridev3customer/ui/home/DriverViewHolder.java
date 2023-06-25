package ro.ananimarius.allridev3customer.ui.home;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ro.ananimarius.allridev3customer.R;

public class DriverViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTextView;
    public TextView surnameTextView;
    public TextView distanceTextView;
    public TextView timeTextView;
    public TextView priceTextView;
    public TextView ratingTextView;

    public DriverViewHolder(@NonNull View itemView, final DriverListAdapter.OnDriverClickListener onDriverClickListener) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.drivers_name);
        surnameTextView = itemView.findViewById(R.id.drivers_surname);
        distanceTextView = itemView.findViewById(R.id.distance);
        timeTextView = itemView.findViewById(R.id.time);
        priceTextView = itemView.findViewById(R.id.price);
        ratingTextView = itemView.findViewById(R.id.drivers_rating);

        LinearLayout driverItemContainer = itemView.findViewById(R.id.driver_item_container);
        driverItemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDriverClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onDriverClickListener.onDriverClick(position);
                    }
                }
            }
        });
    }
}
