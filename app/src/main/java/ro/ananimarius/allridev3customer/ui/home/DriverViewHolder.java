package ro.ananimarius.allridev3customer.ui.home;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ro.ananimarius.allridev3customer.R;

public class DriverViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTextView;

    public DriverViewHolder(@NonNull View itemView, final DriverListAdapter.OnDriverClickListener onDriverClickListener) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.drivers_name);

        itemView.setOnClickListener(new View.OnClickListener() {
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
