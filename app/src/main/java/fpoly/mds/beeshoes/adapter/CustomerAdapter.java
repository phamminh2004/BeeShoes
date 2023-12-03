package fpoly.mds.beeshoes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fpoly.mds.beeshoes.databinding.ItemCustomerBinding;
import fpoly.mds.beeshoes.model.Customer;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Customer> list;

    public CustomerAdapter(Context context, ArrayList<Customer> list) {
        this.context = context;
        this.list = list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomerBinding binding = ItemCustomerBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer customer = list.get(position);
        holder.binding.tvName.setText("Tên: " + customer.getName());
        holder.binding.tvPhone.setText("SĐT: " + customer.getPhone());
        holder.binding.tvAddress.setText("Địa chỉ: " + customer.getAddress());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemCustomerBinding binding;

        public ViewHolder(@NonNull ItemCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
