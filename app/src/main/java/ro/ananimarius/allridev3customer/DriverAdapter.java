//package ro.ananimarius.allridev3customer;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//import ro.ananimarius.allridev3customer.Common.DriverDTO;
//import ro.ananimarius.allridev3customer.R;
//
//public class DriverAdapter extends RecyclerView.Adapter<DriverViewHolder> {
//
//    private List<DriverDTO> drivers;
//
//    public DriverAdapter(List<DriverDTO> drivers) {
//        this.drivers = drivers;
//    }
//
//    @Override
//    public DriverViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drivers_sliding_panel, parent, false);
//        return new DriverViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(DriverViewHolder holder, int position) {
//        // Bind the data to the views in the ViewHolder here
//        DriverDTO driver = drivers.get(position);
//        holder.driverNameTextView.setText(driver.getFirstName());
//        //holder.driverCarTextView.setText(driver.getCar());
//    }
//
//    @Override
//    public int getItemCount() {
//        return drivers.size();
//    }
//}
//
//class DriverViewHolder extends RecyclerView.ViewHolder {
//
//    public TextView driverNameTextView;
//    public TextView driverCarTextView;
//
//    public DriverViewHolder(View itemView) {
//        super(itemView);
//        //driverNameTextView = itemView.findViewById(R.id.driver_name);
////        driverCarTextView = itemView.findViewById(R.id.driver_car_text_view);
//    }
//}
